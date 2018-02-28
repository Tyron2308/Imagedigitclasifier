#!/bin/bash

if [ ! -f streaming-log-analyzer.jar ]; then
	echo Downloading Streaming Log Analyzer
	cp -r /home/ubuntu/real-time-dashboard/StreamingLogAnalyzer/target/ /home/ubuntu/clustertest/script-simulator/ 
fi

echo Submitting Spark job
/usr/local/lib/spark-2.1.0-bin-hadoop2.7/bin/spark-submit --master mesos://18.195.191.222:5050 --class org.sia.loganalyzer.StreamingLogAnalyzer streaming-log-analyzer.jar -brokerList=172.31.13.70:8100 -checkpointDir=file:///tmp/sparkCheckpointDir -inputTopic=test -outputTopic=newtest

