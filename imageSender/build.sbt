import AssemblyKeys._

name := "imageSender"

fork in run := true
scalaVersion := "2.11.8"
val sparkVersion = "2.1.0" 

//ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true)}

resolvers += Resolver.url("bintray-spark-packages", url("http://dl.bintray.com/spark-packages/maven/"))(Resolver.ivyStylePatterns)
resolvers += Resolver.bintrayRepo("cakesolutions", "maven")


libraryDependencies ++= Seq(
	"org.apache.spark" %% "spark-core" % sparkVersion % "provided",
	"org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
	"org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
	"org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",
	"org.apache.spark" %% "spark-streaming-kafka-0-8" % sparkVersion % "provided")

libraryDependencies += "net.cakesolutions" %% "scala-kafka-client" % "1.0.0"
libraryDependencies += "org.apache.kafka" %% "kafka" % "0.11.0.2"

//run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in (Compile, run))
//runMain in Compile <<= Defaults.runMainTask(fullClasspath in Compile, runner in(Compile, run))
mainClass in (Compile, run) := Some("MainTest")

assemblySettings

mergeStrategy in assembly := {
	case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
	case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
	case "log4j.properties"                                  => MergeStrategy.discard
	case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
	case "reference.conf"                                    => MergeStrategy.concat
	case _                                                   => MergeStrategy.first
}


