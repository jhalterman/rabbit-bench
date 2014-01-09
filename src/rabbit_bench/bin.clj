(ns rabbit-bench.bin
  (:gen-class)
  (:require [rabbit-bench.core :as core]
            [rabbit-bench.util :refer :all]
            [clojure.string :refer [join]]
            [clojure.tools.cli :refer [cli]])
  (:import [java.util UUID]))

(defn parse-int [i] (Integer/valueOf i))

(defn- parse-args [args]
  (cli args
       ["-uri" "--uri" "AMQP URI"]
       ["-e" "--exchange" "AMQP exchange to use" :default "test"]
       ["-et" "--exchange-type" "AMQP exchange type" :default "topic"]
       ["-q" "--queue" "AMQP queue to use" :default "test"]
       ["-rk" "--routing-key" "AMQP routing key to use" :default (.toString (UUID/randomUUID))]
       ["-pml" "--producer-msg-limit" "Message limit for each producer" :default 0 :parse-fn parse-int]
       ["-cml" "--consumer-msg-limit" "Message limit for each consumer" :default 0 :parse-fn parse-int]
       ["-tl" "--time-limit" "Producer time limit for benchmark" :default 10000 :parse-fn parse-int]
       ["-rl" "--rate-limit" "Rate limit of messages / second" :default 0 :parse-fn parse-int]
       ["-si" "--sampling-interval" "Sampling interval" :default 1000 :parse-fn parse-int]
       ["-p" "--producer-count" "Producer count" :parse-fn parse-int]
       ["-c" "--consumer-count" "Consumer count" :parse-fn parse-int]
       ["-pc" "--producer-cxn-count" "Producer connection count" :parse-fn parse-int]
       ["-cc" "--consumer-cxn-count" "Consumer connection count" :parse-fn parse-int]
       ["-cpp" "--channel-per-producer" "Creates a channel per producer as opposed to a channel per producer connection" :flag true]
       ["-cpc" "--channel-per-consumer" "Creates a channel per consumer as opposed to a channel per consumer connection" :flag true]))

(defn -main [& args]
  (let [[opts args usage] (parse-args args)
        opts (assoc-if-absent opts :producer-cxn-count (:producer-count opts))
        opts (assoc-if-absent opts :consumer-cxn-count (:consumer-count opts))
        missing-opts (key-diff opts [:uri :exchange :exchange-type :queue :routing-key :time-limit :producer-msg-limit
                                     :consumer-msg-limit :rate-limit :sampling-interval :producer-count :consumer-count])]

    (if (seq missing-opts)
      (do
        (println "Missing required args:" (join ", " (map name missing-opts)) "\n")
        (println usage))
      (do
        (assert-true (<= (:producer-cxn-count opts) (:producer-count opts))
                     "Producer connection count cannot be greater than the producer count")
        (assert-true (<= (:consumer-cxn-count opts) (:consumer-count opts))
                     "Consumer connection count cannot be greater than the consumer count")
        (assert-true (and (not= (:time-limit args) 0) (not= (:message-limit args) 0))
                     "Must specify a time limit or message limit")
        (core/run opts)))))
