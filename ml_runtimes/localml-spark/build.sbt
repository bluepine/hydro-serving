import sbt.Keys._
import sbtassembly.Plugin.AssemblyKeys._

def sparkDependencies(v: String) =
  Seq(
    "org.apache.spark" %% "spark-core" % v,
    "org.apache.spark" %% "spark-sql" % v,
    "org.apache.spark" %% "spark-hive" % v,
    "org.apache.spark" %% "spark-streaming" % v,
    "org.apache.spark" %% "spark-mllib" % v
  )

lazy val hdfsDependencies = Seq(
  "org.apache.hadoop" % "hadoop-client" % "2.6.4",
  "org.apache.hadoop" % "hadoop-hdfs" % "2.6.4",
  "org.apache.hadoop" % "hadoop-common" % "2.6.4"
)

lazy val akkaDependencies = {
  val akkaV = "2.4.14"
  val akkaHttpV = "10.0.0"
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-xml" % akkaHttpV,
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "ch.megard" %% "akka-http-cors" % "0.1.10"
  )
}

lazy val sparkMlServingDependency = RootProject(uri("git://github.com/Hydrospheredata/spark-ml-serving.git"))

lazy val root = project.in(file("."))
  .settings(assemblySettings)
  .dependsOn(sparkMlServingDependency)
  .settings(
    name := "spark-localml-serve",
    version := "1.0",
    scalaVersion := "2.11.8",
    mainClass in assembly := Some("io.hydrosphere.spark_runtime.Boot")
  )
  .settings(
    libraryDependencies += "io.hydrosphere" %% "spark-ml-serving" % "0.1.1",
    libraryDependencies ++= sparkDependencies("2.1.0"),
    libraryDependencies ++= hdfsDependencies,
    libraryDependencies ++= akkaDependencies
  )
  .settings(commonAssemblySettings)

lazy val commonAssemblySettings = Seq(
  mergeStrategy in assembly := {
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") => MergeStrategy.filterDistinctLines
    case m if m.startsWith("META-INF") => MergeStrategy.discard
    case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
    case PathList("org", "apache", xs@_*) => MergeStrategy.first
    case PathList("org", "jboss", xs@_*) => MergeStrategy.first
    case "about.html" => MergeStrategy.rename
    case "reference.conf" => MergeStrategy.concat
    case PathList("org", "datanucleus", xs@_*) => MergeStrategy.discard
    case _ => MergeStrategy.first
  },
  test in assembly := {}
)
