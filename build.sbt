ThisBuild / version := "0.1.0-SNAPSHOT"
name := "AkkaTypedSensorDataProcessor"

ThisBuild / scalaVersion := "2.13.12"
val AkkaVersion = "2.8.4"
val sparkVersion = "3.3.2"

lazy val root = (project in file("."))
  .settings(
    name := "Luxoft Sensor Assignment"
  )

libraryDependencies ++= Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
  "org.apache.spark" %% "spark-core" % sparkVersion)



//Akka dependencies
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)
