(ns rabbit-bench.util
  "All the other things"
  (:require [clojure.set :refer [difference]]))

(defn key-diff 
  "Returns the keys in m that are not present in the key-set"
  [m key-set]
  (difference (set key-set) (set (keys m))))

(defn assoc-if-absent
  "Associates a value with a key if no value is currently present for that key, returning the map"
  [m k v]
  (if (not (m k))
    (assoc m k v)
    m))

(defn assert-true 
  "Asserts that p is true, else throws IllegalArgumentException with the msg"
  [p msg]
  (if-not p (throw (IllegalArgumentException. msg))))

(defn nanos->ms [nanos] (/ nanos 1000000))

(defn millis->secs [millis] (/ millis 1000.0))