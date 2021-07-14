# kafka testing

Testing kafka with java streams and kafka-connect.

## build the project

mvn clean package

## start services

* kafka/zookeeper -> docker-compose exposing zookeeper on 22181,32181,42181 and kafka broker on 29092,39092,49092
* mysql -> docker/listening on port 3308
* sanic-nlp -> docker/listening on port 8000

See projects:

- https://github.com/periket2000/kafka-stack
- https://github.com/periket2000/mysql-stack
- https://github.com/periket2000/sanic-nlp

## create topics

- kafka-topics --zookeeper 127.0.0.1:32181 --topic twitter_status_connect --create --partitions 3 --replication-factor 1
- kafka-topics --zookeeper 127.0.0.1:32181 --topic twitter_deletes_connect --create --partitions 3 --replication-factor 1
- kafka-topics --zookeeper 127.0.0.1:32181 --topic filtered_tweets --create --partitions 3 --replication-factor 1
- kafka-topics --zookeeper 127.0.0.1:32181 --topic reduced_tweets --create --partitions 3 --replication-factor 1

## run it

* connect-standalone connect-standalone-tweeter-source.properties twitter.properties
* java -jar kafka-tweets-filter/target/filter-1.0.jar
* java -jar kafka-tweets-filter/target/map-1.0.jar
* connect-standalone connect-standalone-mysql-sink.properties mysql-sink.properties

## flow

1. tweeter-source feeds kafka topic X
2. filter takes from X and filter by followers feeding topic Y
3. map takes from Y checking tweet language and feeding topic Z
4. mysql-sink takes from Z and insert in mysql DB