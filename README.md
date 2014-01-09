#rabbit-bench

A tool for benchmarking various RabbitMQ configurations.

## Setup

Clone the repo and compile via Leiningen:

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

## License

Copyright © 2013 Jonathan Halterman

Distributed under the Eclipse Public License, the same as Clojure.
