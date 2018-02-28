echo "compile package and dependencies"
sbt package

echo "transfer of binary files from local to hosts"
scp -r tyron@192.168.66.1:/Users/tyron/Desktop/work/clustertest/spark-class-test/target/scala-2.11/sparktest-assembly-0.1-SNAPSHOT.jar ubuntu@slave3:/home/ubuntu/clustertest/spark-class-test/target/scala-2.11/
scp -r tyron@192.168.66.1:/Users/tyron/Desktop/work/clustertest/spark-class-test/src ubuntu@slave3:/home/ubuntu/clustertest/spark-class-test/

