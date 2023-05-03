ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.14"

val sparkVersion = "3.1.3"
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "io.delta" %% "delta-core" % "2.2.0",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.1.1"
)

lazy val root = (project in file("."))
  .settings(
    name := "SparkStructuredStreaming"
  )
