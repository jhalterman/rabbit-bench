(defproject rabbit-bench "0.1.0-SNAPSHOT"
  :description "Benchmarks various RabbitMQ use cases"
  :url "http://github.com/jhalterman/rabbit-bench"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.rabbitmq/amqp-client "3.2.1"]
                 [org.clojure/tools.cli "0.2.4"]
                 [org.clojure/tools.logging "0.2.6"]
                 [incanter/incanter-core "1.4.1"]
                 [incanter/incanter-charts "1.4.1"]]
  :uberjar-name "rabbit-bench.jar"
  :main rabbit-bench.bin)
