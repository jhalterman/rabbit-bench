(ns rabbit-bench.rabbit
  "RabbitMQ related utilities"
  (:import [com.rabbitmq.client ConnectionFactory Connection Channel]))

(defn declare-exchange
  "Declares a non-durable exchange."
  [connection name type]
  (with-open [channel (.createChannel connection)]
    (.exchangeDeclare channel name type false)))

(defn declare-queue
  "Declares a non-durable, non-exclusive, auto-deleting queue."
  [connection name]
  (with-open [channel (.createChannel connection)]
    (.queueDeclare channel name false false true nil)))

(defn bind-queue
  "Binds a queue to an exchange for a routing key."
  [connection exchange queue routing-key]
  (with-open [channel (.createChannel connection)]
    (.queueBind channel queue exchange routing-key)))

(defn ^Connection create-connection
  "Creates a RabbitMQ connection."
  [uri]
  (let [cxn-factory (doto (ConnectionFactory.)
                      (.setUri uri)
                      (.setConnectionTimeout 5000))]
    (println "Creating connection")
    (.newConnection cxn-factory)))

(defn close-connections
  "Quietly closes the connections."
  [cnxs]
  (doseq [c cnxs] (try (.close c)
                       (catch Exception ignore))))

(defn create-connections
  "Returns set of n connections to the uri."
  [n uri]
  (let [cxns (atom nil)]
    (try
      (dotimes [x n]
        (swap! cxns conj (create-connection uri)))
      @cxns
      ; Close connections before throwing
      (catch Exception e
        (close-connections @cxns)
        (throw e)))))

(defn create-channels
  "Returns a lazy sequence of n channels from the connections by cycling through the connections."
  [n connections]
  (map #(.createChannel %) (take n (cycle connections))))