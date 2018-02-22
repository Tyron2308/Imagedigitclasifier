import AssemblyKeys._

name := "streaming-classifier"

fork in run := true
scalaVersion := "2.11.8"
val sparkVersion = "2.1.0" 

//ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true)}

resolvers += Resolver.url("bintray-spark-packages", url("http://dl.bintray.com/spark-packages/maven/"))(Resolver.ivyStylePatterns)
resolvers += Resolver.bintrayRepo("cakesolutions", "maven")

logLevel := Level.Warn

val spark = Seq(
	"org.apache.spark" %% "spark-core" % sparkVersion % "provided",
	"org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
	"org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
	"org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",
	"org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion)
libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

val sequence_database = Seq("org.specs2" %% "specs2-core" % "3.9.1" % "test",
	"com.typesafe.akka" %% "akka-actor" % "2.3.4",
	"com.outworkers" %% "phantom-dsl" % "2.11.2" exclude("org.slf4j" ,"log4j-over-slf4j"),
	"com.outworkers" %% "phantom-connectors" % "2.11.2" exclude("org.slf4j" ,"log4j-over-slf4j"),
	"com.datastax.spark" %% "spark-cassandra-connector" % "2.0.3")

val kafka = Seq("net.cakesolutions" %% "scala-kafka-client" % "1.0.0")

libraryDependencies ++= spark ++ sequence_database ++ kafka
mainClass in (Compile, run) := Some("SparkStreamingClassifier")

assemblySettings



mergeStrategy in assembly := {
	case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
	case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
	case "log4j.properties"                                  => MergeStrategy.discard
	case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
	case "reference.conf"                                    => MergeStrategy.concat
	case _                                                   => MergeStrategy.first
}


mergeStrategy in assembly := {
	case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
	case PathList("javax", "servlet", xs @ _*)    => MergeStrategy.last
	case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
	case PathList("javax", "inject", xs @ _*)     => MergeStrategy.last
	case PathList("org", "aopalliance", xs @ _*)  => MergeStrategy.last
	case PathList("org", "apache", xs @ _*)       => MergeStrategy.last
	case PathList("com", "google", xs @ _*)       => MergeStrategy.last
	case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
	case "plugin.properties" => MergeStrategy.last
	case "log4j.properties" => MergeStrategy.last
	case x =>
		val oldStrategy = (mergeStrategy in assembly).value
		oldStrategy(x)
}
