import java.util.Properties
import kafka.producer.Producer
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig
import org.apache.spark._
import org.apache.spark.streaming._
import _root_.kafka.serializer.StringDecoder
import org.apache.spark.streaming.kafka.KafkaUtils


class SparkStreamingClassifier  {

		val conf 				  						  	= new SparkConf().setAppName("streaming-classifer").setMaster("mesos://18.195.191.222:5050")
		val sparkcontext  	  				  	= new SparkContext(conf)
		val ssc 													= new StreamingContext(sparkcontext, Seconds(1))

		var brokerList: Option[String]		= None
		var checkpointDir: Option[String] = None
		var logsTopic: Option[String] 		= Some("test")
		var statsTopic: Option[String]  	= Some("newtest")
		var SESSION_TIMEOUT_MILLIS 				= 2 * 60 * 1000 //2 minutes
		var numberPartitions 							= 3

	private def printUsageAndExit() {
		System.err.println("Usage: StreamingLogAnalyzer -brokerList=<kafka_host1:port1,...> -checkpointDir=HDFS_DIR [options]\n" +
			"\n" +
			"Options:\n" +
			"  -inputTopic=NAME        Input Kafka topic name for reading logs data. Default is 'weblogs'.\n" +
			"  -outputTopic=NAME       Output Kafka topic name for writing aggregated statistics. Default is 'stats'.\n" +
			"  -sessionTimeout=NUM     Session timeout in minutes. Default is 2.\n" +
			"  -numberPartitions=NUM   Number of partitions for the streaming job. Default is 3.\n")
		System.exit(1)
	}

	private def parseInt(str: String) = try {
		str.toInt
	} catch {
		case e: NumberFormatException => { printUsageAndExit(); 0 }
	}

	private def parseAndValidateArguments(args: Array[String]) {

		args.foreach(arg => arg match {
				case bl if bl.startsWith("-brokerList=") =>
					brokerList = Some(bl.substring(12))
				case st if st.startsWith("-checkpointDir=") =>
					checkpointDir = Some(st.substring(15))
				case st if st.startsWith("-inputTopic=") =>
					logsTopic = Some(st.substring(12))
				case st if st.startsWith("-outputTopic=") =>
					statsTopic = Some(st.substring(13))
			}
		)
		if (brokerList.isEmpty || checkpointDir.isEmpty || logsTopic.isEmpty
			|| statsTopic.isEmpty || SESSION_TIMEOUT_MILLIS < 60 * 1000 || numberPartitions < 1)
			printUsageAndExit()
	}


	def main(args: Array[String])
	{
		parseAndValidateArguments(args)
		val conf = new SparkConf().setMaster("mesos://18.195.191.222:5050")
															.setAppName("Streaming Log Analyzer")
		val ssc = new StreamingContext(conf, Seconds(1))

		ssc.checkpoint(checkpointDir.get)
		println(Console.GREEN + "Starting Kafka direct stream to broker list: "+brokerList.get + Console.WHITE)
		val kafkaReceiverParams = Map[String, String]("metadata.broker.list" -> brokerList.get)
		val kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaReceiverParams, Set(logsTopic.get))


		val grouped = kafkaStream.map(elem =>
			println(Console.GREEN + "elem 1 : " + elem._1 + elem._2 + Console.WHITE)
		)
		ssc.start()
		ssc.awaitTermination()
	}
}

case class KafkaProducerWrapper(brokerList: String)
{
	val producerProps = {
		val prop = new Properties
		prop.put("metadata.broker.list", brokerList)
		prop
	}
	val p = new Producer[Array[Byte], Array[Byte]](new ProducerConfig(producerProps))

	def send(topic: String, key: String, value: String) {
		p.send(new KeyedMessage(topic, key.toCharArray.map(_.toByte), value.toCharArray.map(_.toByte)))
	}
}

object KafkaProducerWrapper {
	var brokerList = ""
	lazy val instance = new KafkaProducerWrapper(brokerList)
}
