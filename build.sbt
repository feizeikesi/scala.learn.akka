name := "scala.learn.akka"

version := "1.0"

scalaVersion := "2.11.8"


val akkaVersion = "2.4.14"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion,
  "com.typesafe.akka" % "akka-remote_2.11" % akkaVersion,
  "com.typesafe.akka" % "akka-cluster_2.11" % akkaVersion,
  "com.typesafe.akka" % "akka-cluster-metrics_2.11" % akkaVersion,
  "com.typesafe.akka" % "akka-cluster-tools_2.11" % akkaVersion
)

