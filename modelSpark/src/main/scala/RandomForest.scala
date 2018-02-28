import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.tree.configuration.Strategy
import org.apache.spark.mllib.tree.RandomForest


object RunDecisionTree extends ModelSpark {

  private val set = createLabeledPoint(Seq("hdfs://172.31.0.217:8088/test.csv",
    "hdfs://172.31.0.217:8088/mnist_test2.csv"))

  val train = set(0).cache()
  val test = set(1).cache()

  def runRandomForest(dataPoints: RDD[LabeledPoint],
                      testPoint: RDD[LabeledPoint]): RDD[(Double, Double)] = {
 
        val numClasses = 10
        val categoricalFeaturesInfo = Map[Int, Int]()
        val impurity = "gini"
        val treeStrategy = Strategy.defaultStrategy("Classification")
        val numTrees = 5
        val featureSubsetStrategy = "auto"
        val model   = RandomForest.trainClassifier(dataPoints, 20,
          categoricalFeaturesInfo,  numTrees, featureSubsetStrategy, impurity, 5, 32, seed = 12345)
        val Err     = testPoint.map { point =>
        val pred    = model.predict(point.features)
        (point.label, pred) }
        val testErr = Err.filter(r => r._1 != r._2 ).count().toDouble / testPoint.count()
        val error   = Err.filter(r => r._1 != r._2).count().toDouble
        val good    = Err.filter(r => r._1 == r._2).count().toDouble
 
        println(Console.GREEN + "nombre de bon resultat : " + good)
        println(Console.GREEN + "faux resultat         : "+ error + Console.WHITE)
        println(Console.GREEN + "Test Error = " + testErr + Console.WHITE)
    Err
    }
  val results = runRandomForest(train, test)
  printResultat(results)
}


object RunRandomForest extends ModelSpark {

  private val set = createLabeledPoint(Seq("hdfs://172.31.0.217:8088/test.csv",
    "hdfs://172.31.0.217:8088/mnist_test2.csv"))

  val train = set(0).cache()
  val test  = set(1).cache()

  def runRandomForest(dataPoints: RDD[LabeledPoint], testPoint: RDD[LabeledPoint])
   : RDD[(Double, Double)] = {
        val numClasses = 10
        val categoricalFeaturesInfo = Map[Int, Int]()
        val impurity      = "gini"
        val treeStrategy  = Strategy.defaultStrategy("Classification")
        val numTrees      = 5
        val featureSubsetStrategy = "auto"

        val model         = RandomForest.trainClassifier(dataPoints, 20, categoricalFeaturesInfo,
		numTrees, featureSubsetStrategy, impurity, 5, 32, seed = 12345)

        val Err   = testPoint.map { point =>
        val pred  = model.predict(point.features)
        (point.label, pred) }
 
        val testErr = Err.filter(r => r._1 != r._2 ).count().toDouble / testPoint.count()
        val error   = Err.filter(r => r._1 != r._2).count().toDouble
        val good    = Err.filter(r => r._1 == r._2).count().toDouble
 
        println(Console.GREEN + "nombre de bon resultat : " + good)
        println(Console.GREEN + "faux resultat          : "+ error + Console.WHITE)
        println(Console.GREEN + "Test Error = " + testErr + Console.WHITE)
        model.save(sparkContext, "hdfs://172.31.0.217:8088/randomForest")
      Err
    }
   val results = runRandomForest(train, test)
  printResultat(results)
}


class ModelSpark extends Serializable {

  @transient lazy val sparkconf     = new SparkConf().setMaster("mesos://18.195.191.222:5050")
                                                     .setAppName("traindigit")

  @transient lazy val sparkContext  = new SparkContext(sparkconf)
  @transient lazy val sqlContext    = new org.apache.spark.sql.SQLContext(sparkContext)

  def createLabeledPoint(seq: Seq[String]): Seq[RDD[LabeledPoint]] = {

    val tmp = for (str <- seq) yield {
      sparkContext.textFile(str).map(elem => elem.split(","))
    }
    tmp.map(rdd =>
    rdd.map { row =>
        new LabeledPoint(row.head.toDouble,
          Vectors.dense(row.tail.map(r => r.toDouble)))
    })
  }

  def printResultat(results: RDD[(Double, Double)]): Unit = {
      results.foreach { tuple =>
        println("label: " + tuple._1)
        println("prediction: " + tuple._2)
      }
  }

  def saveToFile(rdd: RDD[(Double, Double)], file: String, format: String): Unit = {
        import sqlContext.implicits._
        rdd.toDF().write.format(format).option("header", "true").save(file)
  }

  def stopSparkContext(): Unit = {
    sparkContext.stop()
  }

  }
