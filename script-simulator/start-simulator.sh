#!/bin/bash
#download the jar
if [ ! -e kafka-logs-simulator.jar ] then
	cp -r ../real-time-dashboard/KafkaLogsSimulator/kafka-logs-simulator.jar . 

if [[ $@ == *-brokerList=* ]] ; then
	java -jar kafka-logs-simulator.jar $@
else
	java -jar kafka-logs-simulator.jar -brokerList=172.31.13.70:8100 $@
fi

