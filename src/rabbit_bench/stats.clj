(ns rabbit-bench.stats
  "Computes and reports benchmark stats"
  (:require [rabbit-bench.util :refer :all])
  (:import [java.util.concurrent TimeUnit]
           [java.util.Scanner]))

(defprotocol Stats
  (mark-send [this] "Marks a message as having been sent")
  (mark-receive [this latency] "Marks a message as having been received")
  (create-sample [this] "Creates and prints a sample")
  (print-report [this] "Prints a report of the statistics"))

(defn create-stats []
  (let [total-send-count (atom 0)
        interval-send-count (atom 0)
        total-receive-count (atom 0)
        interval-receive-count (atom 0)
        min-latency (atom Long/MAX_VALUE)
        max-latency (atom Long/MIN_VALUE)
        total-latency-sum (atom 0)
        interval-latency-sum (atom 0)
        start-time (System/currentTimeMillis)
        send-start-time (atom 0)
        receive-start-time (atom 0)
        last-stats-time (atom start-time)]

    (reify Stats
      (mark-send [this]
        (if (= 0 @send-start-time)
          (reset! send-start-time (System/currentTimeMillis)))
        (swap! total-send-count inc)
        (swap! interval-send-count inc))

      (mark-receive [this latency]
        (if (= 0 @receive-start-time)
          (reset! receive-start-time (System/currentTimeMillis)))
        (swap! total-receive-count inc)
        (swap! interval-receive-count inc)
        (swap! min-latency min latency)
        (swap! max-latency max latency)
        (swap! total-latency-sum + latency)
        (swap! interval-latency-sum + latency))

      (create-sample [this]
        (let [now (System/currentTimeMillis)
              total-elapsed (- now start-time)
              interval-elapsed (- now @last-stats-time)
              send-rate (/ @interval-send-count interval-elapsed)
              receive-rate (/ @interval-receive-count interval-elapsed)
              avg-latency (/ @interval-latency-sum @interval-receive-count)]
          (when (not= 0 @interval-receive-count)
            (println (format "Time: %.3fs, Sent: %1.2f msg/s, Received: %1.2f msg/s, Min/Avg/Max latency: %d/%d/%d ms"
                             (millis->secs total-elapsed)
                             (* send-rate 1000.0)
                             (* receive-rate 1000.0)
                             (long (nanos->ms @min-latency))
                             (long (nanos->ms avg-latency))
                             (long (nanos->ms @max-latency)))))
          (reset! last-stats-time now))
        (reset! interval-send-count 0)
        (reset! interval-receive-count 0)
        (reset! min-latency Long/MAX_VALUE)
        (reset! max-latency Long/MIN_VALUE)
        (reset! interval-latency-sum 0))

      (print-report [this]
        (let [now (System/currentTimeMillis)
              send-elapsed (- now @send-start-time)
              receive-elapsed (- now @receive-start-time)
              send-rate (/ @total-send-count send-elapsed)
              receive-rate (/ @total-receive-count receive-elapsed)
              latency (/ @total-latency-sum @total-receive-count)]
          (println (format "\nAvg Send Rate: %1.2f msg/s, Avg Receive Rate: %1.2f msg/s, Avg latency: %d ms"
                           (* send-rate 1000.0)
                           (* receive-rate 1000.0)
                           (long (nanos->ms latency)))))))))