import com.typesafe.sbt.packager.docker._

name := "dockerized_akka_cluster"

version := "1.0"

scalaVersion := "2.11.7"

val akkaVersion = "2.4.1"

lazy val commonSettings = Seq(
  organization := "com.fiser.akka",
  version := "1.0",
  scalaVersion := "2.11.7"
)

val commonDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion
)

enablePlugins(DockerPlugin)
enablePlugins(JavaAppPackaging)

lazy val root = (project in file(".")).
  aggregate(core, frontend, backend)

lazy val core = project.in(file("core")).
  settings(commonSettings: _*)

lazy val frontend = project.settings(commonSettings: _*).dependsOn(core)
  .settings(
    libraryDependencies ++= commonDependencies
  )

lazy val backend = project.in( file("backend") ).
  settings(commonSettings: _*).dependsOn(core)
  .settings(
    libraryDependencies ++= commonDependencies
  )

    