import java.util.{Properties, Random}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.{SparkConf, SparkContext}

object ImageSender {

  private var ipAddress: Option[String] = None
  private var sessionId: Option[String] = None
  private var timeToRun                 = 0
  private val random                    = new Random
  private val brokerList                = "172.31.13.70:8100"
  private lazy val props                = new Properties()
  private val TOPIC_NAME                = "test"

  props.put("bootstrap.servers", "172.31.13.70:8100")
  props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("client.id", "something")

  val producer = new KafkaProducer[Int, String](props)

  def ConstructSender(iPAddressName: String, sessionIdd: String, timetoRunn: Int): Unit = {

    ipAddress = Some(iPAddressName)
    sessionId = Some(sessionIdd)
    timeToRun = timetoRunn
  }

  def sendImgae(): Unit = {

    println(Console.GREEN + " send Image " + Console.WHITE)
    val conf = new SparkConf().setAppName("send-image").setMaster("mesos://18.195.191.222:5050")
    val sparkContext = new SparkContext(conf)
    val images = sparkContext.textFile("hdfs://172.31.0.217:8088/test.csv").zipWithUniqueId().collect()

    println(Console.GREEN + "image : " + images.length + Console.WHITE)

    val startTime = System.currentTimeMillis()
    var start = true
    var count = 0

    println(Console.GREEN + "send image : " + timeToRun + Console.WHITE)

    while (start) {
      val currTime = System.currentTimeMillis
      println(Console.GREEN + (currTime - startTime) + Console.WHITE)

      if (currTime - startTime >= timeToRun) start = false
      else {
        val num = random.nextInt()
        if (num <= images.length && num >= 0) {
          val img = images(num)._1
          val record = new ProducerRecord(TOPIC_NAME, 1, img)
          producer.send(record)
          count += 1
          println(Console.RED + " count: " + count + Console.WHITE)
        }
      }
    }

    println(Console.GREEN + " --> " + count  + Console.WHITE)
    producer.close()
  }
}
