#!/bin/bash

#[ $# -lt 2 ] && printf "\nTwo arguments needed: Zookeeper address and the Kafka topic name\n\n" && exit 1

cntrunning=`docker ps | grep sia-dashboard | awk '{print $2}'`
[ "$cntrunning" == "sia-dashboard" ] && echo "sia-dashboard already running" && exit 1

existing=`docker images | grep sia-dashboard | awk '{print $1}'`
if [ "$existing" != "sia-dashboard" ]
then
	cd dashboard-image
	docker build -t sia-dashboard .
	cd ..
fi

docker run -d -p 8288:8288 -e "ZKADDR=zk://18.196.20.136:2181,18.195.209.31:2181,35.157.78.251:2181/mesos" -e "TOPIC=newtest" sia-dashboard

