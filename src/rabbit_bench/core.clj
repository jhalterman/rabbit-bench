(ns rabbit-bench.core
  (:require [rabbit-bench.rabbit :refer :all]
            [rabbit-bench.stats :refer :all]
            [clojure.java.io :refer [output-stream]])
  (:import [com.rabbitmq.client DefaultConsumer MessageProperties Connection Channel]
           [java.io ByteArrayOutputStream DataOutputStream ByteArrayInputStream DataInputStream]))

(def running (atom true))

(defn- create-msg
  [msg-number]
  (let [out (ByteArrayOutputStream.)
        dout (doto (DataOutputStream. out) (.writeInt msg-number) (.writeLong (System/nanoTime)) (.flush))]
    (.toByteArray out)))

(defn- calc-pause [rate-limit msg-count elapsed]
  "Returns some amount of milliseconds to pause based on the rate limit, msg count, and amount of elapsed time"
  (if (not= rate-limit 0)
    (-> (* msg-count 1000) (/ rate-limit) (- elapsed))
    0))

(defn- is-within-limits
  "Returns true of the time-elapsed and msg-count are within the time-limit and msg-limit"
  [time-limit msg-limit time-elapsed msg-count]
  (and (or (= time-limit 0)
           (< time-elapsed time-limit))
       (or (= msg-limit 0)
           (< msg-count msg-limit))))

(defn- create-producer
  "Creates a producer that sends messages until the time-limit or msg-limit are exceeded,
  pausing throughout according to the rate-limit"
  [channel exchange routing-key msg-limit time-limit rate-limit stats]
  (println "Creating producer")
  (let [start-time (System/currentTimeMillis)]
    (loop [msg-count 0
           now start-time]
      (let [elapsed (- now start-time)]
        (when (is-within-limits time-limit msg-limit elapsed msg-count)
          ; Pause for rate limiting
          (let [pause (calc-pause rate-limit msg-count elapsed)]
            (if (> pause 0)
              (Thread/sleep pause)))
          (.basicPublish channel exchange, routing-key, MessageProperties/MINIMAL_BASIC, (create-msg msg-count))
          (mark-send stats)
          (recur (inc msg-count) (System/currentTimeMillis)))))))

(defn- create-consumer
  "Creates a consumer for the channel that consumes until the msg and/or time limits have been exceeded, and
  increments the stats. Returns the consumer's tag."
  [channel exchange queue routing-key msg-limit time-limit stats]
  (println "Creating consumer")
  (let [start-time (System/currentTimeMillis)
        msg-count (atom 0)
        consumer (proxy [DefaultConsumer] [channel]
                   (handleDelivery [consumer-tag envelope properties body]
                     (let [elapsed (- (System/currentTimeMillis) start-time)]
                       (swap! msg-count inc)
                       (when (is-within-limits time-limit msg-limit elapsed msg-count)
                         (let [dis (-> (ByteArrayInputStream. body) (DataInputStream.))
                               msg-num (.readInt dis)
                               msg-nanos (.readLong dis)
                               latency (- (System/nanoTime) msg-nanos)]
                           (mark-receive stats latency))))))]
    (.basicConsume channel queue true consumer)))

(defn- print-stats
  "Publishes stats"
  [stats sampling-interval]
  (while @running
    (create-sample stats)
    (Thread/sleep sampling-interval))
  (print-report stats))

(defn run
  "Runs a RabbitMQ benchmark"
  [args]
  (let [{:keys [uri producer-count consumer-count producer-cxn-count consumer-cxn-count exchange exchange-type
                queue routing-key producer-msg-limit consumer-msg-limit time-limit rate-limit sampling-interval]} args
        ; Pre-create all connections together in case there's a failure
        connections (create-connections (+ producer-cxn-count consumer-cxn-count) uri)
        producer-cxns (take producer-cxn-count connections)
        consumer-cxns (take-last consumer-cxn-count connections)
        producer-channels (create-channels (if (:channel-per-producer-cxn args)
                                             producer-cxn-count
                                             producer-count)
                                           producer-cxns)
        consumer-channels (create-channels (if (:channel-per-consumer-cxn args)
                                             consumer-cxn-count
                                             consumer-count)
                                           consumer-cxns)
        stats (create-stats)
        producers (map #(partial create-producer % exchange routing-key producer-msg-limit time-limit rate-limit stats)
                       (take producer-count (cycle producer-channels)))
        consumers (map #(create-consumer % exchange queue routing-key consumer-msg-limit time-limit stats)
                       (take consumer-count (cycle consumer-channels)))
        producer-threads (map-indexed (fn [i t] (Thread. t (str "producer-" i))) producers)
        admin-cxn (create-connection uri)]

    (try
      ; Setup
      (reset! running true)
      (dorun connections)
      (declare-exchange admin-cxn exchange exchange-type)
      (declare-queue admin-cxn queue)
      (bind-queue admin-cxn exchange queue routing-key)

      ; Run
      (dorun consumers)
      (doseq [t producer-threads] (.start t))
      (doto (Thread. #(print-stats stats sampling-interval)) (.start))
      (doseq [t producer-threads] (.join t))

      ; Cleanup
      (finally
        (reset! running false)
        (close-connections connections)
        (.close admin-cxn)))))
