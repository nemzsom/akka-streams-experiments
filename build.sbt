name := "akka-streams-experiments"

version := "1.0"

scalaVersion := "2.11.7"

organization := "hu.nemzsom"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Xfuture",
  "-Ywarn-unused-import"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test",
  "com.typesafe.akka" %% "akka-stream-experimental" % "1.0",
  "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
  "net.liftweb" %% "lift-webkit" % "3.0-M5-1",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4-M3",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.12",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.4-M3",
  "com.typesafe.akka" % "akka-stream-testkit-experimental_2.11" % "1.0"
)

libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-log4j12")) }

libraryDependencies ~= { _.map(_.exclude("log4j", "log4j")) }

wartremoverErrors ++= Seq(
  Wart.Any2StringAdd,
  Wart.EitherProjectionPartial,
  Wart.OptionPartial,
  Wart.Product,
  Wart.Serializable,
  Wart.ListOps,
  Wart.Return
)

ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 70

ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := false

ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := {
  if(scalaBinaryVersion.value == "2.11") true
  else false
}

test in assembly := {}

assemblyJarName in assembly := "pinterest-indexer.jar"

publishArtifact in Test := false

parallelExecution in Test := false
