import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import java.util.{Properties, UUID}

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.dstream.InputDStream

import scala.concurrent.ExecutionContext.Implicits.global

class SparkStreamingClassifier(val brokerList: String, val topic : Set[String]) extends Serializable {

	val checkpointDir: String 					= "hdfs://172.31.0.217:8088/sparkCheckpointDir"
	val store: String 									= "answerclassifier"
	val SESSION_TIMEOUT_MILLIS: Long 		= 2 * 60 * 1000 //2 minutes
	val numberPartitions: Int 					= 1
	val conf 														= new SparkConf().setAppName("streaming-classifer")
			                                                 .setMaster("mesos://18.195.191.222:5050")
		                                                   .set("spark.mesos.coarse", "false")
		                                                   .set("spark.cassandra.connection.host", "172.31.13.70")

	DmpDatabase.create()
	val textOutputWriter								= DmpDatabase.users



	def createConsumerConfig(): Properties = {
		val props = new Properties()
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList)
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
		props
	}

	def createKafkaDStream(ssc: StreamingContext, acks: String, zk_ens: String, topic: Set[String]): Option[InputDStream[ConsumerRecord[String, String]]] = {
		val kafkaReceiveParams = Map[String, String]("bootstrap.servers" -> brokerList, "zookeeper.connect" -> zk_ens,
																									"metadata.broker.list" -> brokerList,
      																						"group.id" -> s"${UUID.randomUUID().toString}",
																									ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest",
																								 	"key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
		                                             	"value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer")

		Some(KafkaUtils.createDirectStream[String, String](ssc, PreferConsistent, Subscribe[String, String](topic, kafkaReceiveParams)))
	}



	def runPredictionStream(kafkaStream: InputDStream[ConsumerRecord[String, String]],
                          model: DecisionTreeModel, ssc: StreamingContext) = {
		println(Console.GREEN + "stream kafka bus " + Console.WHITE)
		val predictions = kafkaStream.transform { elem =>
			elem.map {
				r =>
					val to_split = r.value().split(",")
				  val data = new LabeledPoint(to_split.head.toDouble, Vectors.dense(to_split.tail.map(_.toDouble)))
				  val result = model.predict(data.features)
				  (result, data.label)
			}
		}

		predictions.foreachRDD(rdd => {
			rdd.isEmpty() match {
				case true =>
					println(Console.GREEN + "rdd empty" + Console.WHITE)
				case false =>
					println(Console.BLUE + "rdd pas  empty " + Console.WHITE)
					rdd.foreach(iterator => {
					KafkaProducerWrapper.brokerList		  = brokerList
					val producer 												= KafkaProducerWrapper.instance
					println(Console.GREEN + iterator.toString() + Console.WHITE)
						producer.send(store, "dddd", iterator.toString())
					})
			}
		})
    ssc.start()
    ssc.awaitTermination()
	}



	@throws(classOf[RuntimeException])
	def runStreamerTopic(topic: Set[String], ssc: StreamingContext, zk_ens: String, sqlContext: SQLContext): Unit = {
		val kafkaStream = createKafkaDStream(ssc, "ack", zk_ens, topic)


		val df = sqlContext.read.format("org.apache.spark.sql.cassandra").option("table", "emp").option("keyspace", "sparktestcassandra").load()


		kafkaStream.isDefined match {
			case true =>
				kafkaStream.get.foreachRDD { rdd =>
          rdd.isEmpty() match {
						case true => println(Console.GREEN + "rdd empty" + Console.WHITE)
						case false =>
							rdd.foreach {
								iterator =>
									val to_split = iterator.value().split(",")
									println(Console.GREEN + "runStreamTopic" + Console.WHITE)
									textOutputWriter.store(new Record(UUID.randomUUID(), List.empty[Double], to_split.head, to_split.reverse.head))
							}
					}
				}
			case false => throw new Exception("kafkaStream is not defined")
		}

		ssc.start()
    ssc.awaitTermination()
	}
}

object SparkStreamingClassifier {

	def printUsage(): Unit = {
		println(Console.RED + "You made a mistake with the argument given to this program")
		println(Console.GREEN + " args[0]: zookeeper_ens:port/path" + Console.WHITE)
		println(Console.GREEN + " args[1]: broker:port " + Console.WHITE)
		println(Console.GREEN + " args[2]: topic-input " + Console.WHITE)
		println(Console.GREEN + " args[3]: hdfs-directory-path" + Console.WHITE)
	}

	///TODO parsing function for argument receive at the execution
	def parseArgument(args: Array[String]): Boolean = {
		assert(args.length >= 4, "argument lenght failed /n" + printUsage())
		true
	}

	def main(args: Array[String]): Unit = {

		assert(parseArgument(args), printUsage())

		try {
			val consumerWrapper = new SparkStreamingClassifier(args(1), Set(args(2)))
			val sparkcontext		= new SparkContext(consumerWrapper.conf)
			val ssc 			 			= new StreamingContext(sparkcontext, Seconds(10))
			val sqlContext 			= new SQLContext(sparkcontext)


			val hdfs_path 			= "hdfs://172.31.0.217:8088/"

      ssc.checkpoint(consumerWrapper.checkpointDir)
			val kafkaStream 		= consumerWrapper.createKafkaDStream(ssc, "acks", args(0), Set(args(2)))

      if (kafkaStream.isDefined){
        val model 				= DecisionTreeModel.load(sparkcontext, hdfs_path + args(3))
				kafkaStream.isDefined match {
					case true =>
						consumerWrapper.runPredictionStream (kafkaStream.get, model, ssc)
						//consumerWrapper.producer.producer.close()
						//consumerWrapper.runStreamerTopic(Set("answerclassifier"), ssc, args(0), sqlContext)
					case false => throw new Exception
				}
			}
		}
		catch {
			case e: Exception =>
				println(Console.GREEN + " exception raised : " + e.printStackTrace() + Console.WHITE)
		}
	}
}

case class KafkaProducerWrapper(brokerList: String) extends Serializable
{
	val producerProps = {
		val props = new Properties
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
		props.put("metadata.broker.list", brokerList)
		props.put("bootstrap.servers", "172.31.13.70:8100")
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
		props
	}
	val producer = new KafkaProducer[String, String](producerProps)

	def send(topic: String, key: String, value: String) {
		producer.send(new ProducerRecord(topic, key, value))
	}

	def stop() = {
		producer.close()
	}
}

object KafkaProducerWrapper {
	var brokerList = ""
	lazy val instance = new KafkaProducerWrapper(brokerList)
}
