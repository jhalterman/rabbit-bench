#rabbit-bench

A tool for benchmarking various RabbitMQ configurations. Currently measures latency and throughput to a single queue bound to a single exchange using a variable number of producer and consumer connections and channels.

## Setup

Clone the repo and compile via [Leiningen](http://leiningen.org/):

```
lein uberjar
```

## Usage

```
java -jar target/rabbit-bench.jar
```

Options:

```
-uri, --uri                                            
-e, --exchange                                         
-et, --exchange-type                                   
-q, --queue                                            
-rk, --routing-key                                     
-pml, --producer-msg-limit                             
-cml, --consumer-msg-limit                             
-tl, --time-limit                                      
-rl, --rate-limit                                      
-si, --sampling-interval                               
-p, --producer-count                                   
-c, --consumer-count                                   
-pc, --producer-cxn-count                              
-cc, --consumer-cxn-count                              
-cpp, --no-channel-per-producer, --channel-per-producer
-cpc, --no-channel-per-consumer, --channel-per-consumer

```

### Example

Let's run a benchmark that creates 3 producers and consumers, each with their own connection:

```
$ java -jar target/rabbit-bench.jar -uri amqp://user:pass@10.10.10.10:5672 -p 3 -c 3 -tl 5000
Creating connection
Creating connection
Creating connection
Creating connection
Creating connection
Creating connection
Creating connection
Creating consumer
Creating consumer
Creating consumer
Creating publisher
Creating publisher
Creating publisher

Time: 0.774s, Sent: 2363.05 msg/s, Received: 103.36 msg/s, Min/Avg/Max latency: 32/77/94 ms
Time: 1.781s, Sent: 3802.38 msg/s, Received: 1693.15 msg/s, Min/Avg/Max latency: 56/479/998 ms
Time: 2.782s, Sent: 1794.21 msg/s, Received: 1910.09 msg/s, Min/Avg/Max latency: 937/1400/1952 ms
Time: 3.783s, Sent: 1878.12 msg/s, Received: 1873.13 msg/s, Min/Avg/Max latency: 1081/1800/2905 ms
Time: 4.784s, Sent: 1930.07 msg/s, Received: 1963.04 msg/s, Min/Avg/Max latency: 1255/1820/3600 ms
Time: 5.785s, Sent: 1758.24 msg/s, Received: 1324.68 msg/s, Min/Avg/Max latency: 1209/2372/4751 ms

Avg Send Rate: 2165.66 msg/s, Avg Receive Rate: 1455.81 msg/s, Avg latency: 1534 ms
```

Another run with 10 producers and consumers, each with their own channel shared across 3 consumer and producer connections:

```
$ java -jar target/rabbit-bench.jar -uri amqp://user:pass@10.10.10.10:5672 -p 10 -c 10 -pc 3 -cc 3 -tl 5000
```

Another run with 10 producers and consumers, each sharing a single channel across 3 consumer and producer connections:

```
$ java -jar target/rabbit-bench.jar -uri amqp://user:pass@10.10.10.10:5672 -p 10 -c 10 -pc 3 -cc 3 --no-channel-per-producer --no-channel-per-consumer -tl 5000
```

## License

Copyright © 2013 Jonathan Halterman

Distributed under the Eclipse Public License, the same as Clojure.