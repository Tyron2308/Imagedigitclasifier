import java.util.logging.{Level, Logger}

object Maintest {

  def main(args: Array[String]): Unit = {

    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)
    Logger.getLogger("org.apache.spark.storage.BlockManager")
          .setLevel(Level.OFF)
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    println(Console.GREEN + "send image via Kafka" + Console.WHITE)
    assert(args.length == 4, "program need to be executed with 3 arguments : --brokerList: host:port --hdfs_path_file_image -- OutputTopic")

    val sender = new ImageSender(args(0), "hdfs://172.31.0.217:8088/"+ args(1), args(2), args(3))
    sender.sendImage("mesos://18.195.191.222:5050", (2 * 60 * 1000))
  }

}
