import java.io.FileNotFoundException
import java.util.{Properties, Random, UUID}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.{SparkConf, SparkContext}

class ImageSender(brokerList: String, imageToSend: String,
                  topicInput: String, zookeeper_ens: String) {

  private val random      = new Random
  private lazy val props  = new Properties()

  assert(brokerList.contains(":"), "format brokerList: host:port")
  props.put("bootstrap.servers", brokerList)
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("client.id", s"${UUID.randomUUID().toString}")

  val producer = new KafkaProducer[String, String](props)

  def sendImage(mesos_ip: String, timeToRun: Long): Unit = {

    try {

    val conf         = new SparkConf().setAppName("send-image")
                                      .setMaster(mesos_ip)
    val sparkContext = new SparkContext(conf)
    val images       = sparkContext.textFile(imageToSend).zipWithUniqueId().collect()
    val startTime    = System.currentTimeMillis()
    var count        = 0
    while ((startTime - System.currentTimeMillis()) <= timeToRun) {
      val num        = random.nextInt() % images.length
      if (num <= images.length && num >= 0) {
         val img     = images(num)._1
         println(Console.RED + "send image motherfucker to the topic : " + topicInput + Console.WHITE)
         val record  = new ProducerRecord(topicInput, count.toString, img)
         producer.send(record)
         count += 1
      }
    }
    producer.close()
  } catch {
      case file : FileNotFoundException => println(Console.RED + "file does not exist " + Console.WHITE)
      case task : ExceptionInInitializerError => println(Console.RED + task.printStackTrace() + Console.WHITE) }
  }
}
