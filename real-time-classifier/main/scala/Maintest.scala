import org.apache.spark.mllib.tree.RandomForest
import RandomForest._
import ImageSender._

object Maintest {

  def main(args: Array[String]): Unit = {

    println(Console.GREEN + "creer labeledpoint" + Console.WHITE)

    //val tmp = RunDecisionTree
    //tmp.saveToFile(tmp.results, "toot.csv", "com.databricks.spark.csv")
    //tmp.stopSparkContext()

    val sender = ImageSender

    sender.ConstructSender("172.31.13.70", "ssID2", (2 * 60 * 1000))
    sender.sendImgae()
  }

}
