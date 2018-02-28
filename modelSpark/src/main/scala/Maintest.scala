object Maintest {

  def main(args: Array[String]): Unit = {

    println(Console.GREEN + "creer labeledpoint" + Console.WHITE)

    val tmp = RunRandomForest
    tmp.stopSparkContext()
  }
}
