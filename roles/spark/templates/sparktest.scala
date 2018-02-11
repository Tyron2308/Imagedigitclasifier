import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import scala.collection.mutable.ListBuffer
import org.apache.spark.rdd.RDD

object maintest {

def main(array: Array[String]): Unit = {

  val master = "{{hostvars[groups['mesos_masters'][0]]['inventory_hostname']}}"
  val conf = new SparkConf().setMaster("mesos://"+master).setAppName("app")

  val sc = new SparkContext(conf)

  println(Console.GREEN + "This program fetch many file and count the number of line per file for summing it after. " + Console.WHITE)
  val files = Seq("hdfs://{{hostvars[groups['mesos_masters'][0]]['ansible_default_ipv4']['address']}}:8088/log/mysql/current_dept_emp.2017-12-06T_18.ip-172-31-18-243",
		"hdfs://{{hostvars[groups['mesos_masters'][0]]['ansible_default_ipv4']['address']}}:8088/log/mysql/dept_emp_latest_date.2017-12-06T_18.ip-172-31-18-243",
		"hdfs://{{hostvars[groups['mesos_masters'][0]]['ansible_default_ipv4']['address']}}:8088/log/mysql/dept_manager.2017-12-06T_18.ip-172-31-18-243",
		"hdfs://{{hostvars[groups['mesos_masters'][0]]['ansible_default_ipv4']['address']}}:8088/log/mysql/employees.2017-12-06T_18.ip-172-31-18-243",
		"hdfs://{{hostvars[groups['mesos_masters'][0]]['ansible_default_ipv4']['address']}}:8088/log/mysql/salaries.2017-12-06T_18.ip-172-31-18-243",
		"hdfs://{{hostvars[groups['mesos_masters'][0]]['ansible_default_ipv4']['address']}}:8088/log/mysql/titles.2017-12-06T_18.ip-172-31-18-243")

  val filetest = sc.textFile("filetest")
  val list: ListBuffer[RDD[String]] = ListBuffer[RDD[String]]()
  for (file <- files)
  	list += sc.textFile(file)

  list.map { 	case rdd: RDD[String] => rdd.collect().foreach(println)
  		case _ => println("empty")  }
  println(Console.GREEN + "close console " + Console.WHITE)
  }
}
