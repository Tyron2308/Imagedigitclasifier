#!/bin/bash
K=`ps -ef | grep kafka | grep server.properties | grep -v grep | awk '{print $2}'`
if [ "$K" == "" ]; then
	echo Starting Kafka
	/usr/local/lib/kafka_2.11-0.11.0.2/bin/kafka-server-start.sh /usr/local/lib/kafka_2.11-0.11.0.2/config/server.properties &
else
	echo Kafka already started
fi

sleep 3
T=`/usr/local/lib/kafka_2.11-0.11.0.2/bin/kafka-topics.sh --list --zookeeper 18.196.20.136:2181,18.195.209.31:2181,35.157.78.251:2181 | grep images-sender`
if [ "$T" == "" ]; then
	echo Creating weblogs topic
	/usr/local/lib/kafka_2.11-0.11.0.2/bin/kafka-topics.sh --create --topic imagessender --replication-factor 1 --partitions 1 --zookeeper 18.196.20.136:2181,18.195.209.31:2181,35.157.78.251:2181 
fi
T=`/usr/local/lib/kafka_2.11-0.11.0.2/bin/kafka-topics.sh --list --zookeeper 18.196.20.136:2181,18.195.209.31:2181,35.157.78.251:2181 grep answer_classifier`
if [ "$T" == "" ]; then
	echo Creating stats topic
	/usr/local/lib/kafka_2.11-0.11.0.2/bin/kafka-topics.sh --create --topic answerclassifier --replication-factor 1 --partitions 1 --zookeeper :18.196.20.136:2181,18.195.209.31:2181,35.157.78.251:2181
fi

##/usr/local/lib/kafka_2.11-0.11.0.2/kafka_2.11-0.11.0.2/bin/kafka-console-consumer.sh --bootstrap-server 172.31.13.70:8100 --topic test --from-beginning
