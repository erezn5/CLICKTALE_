name := "com.clicktale.pipeline.regressions"
version := "2.0.2"
scalaVersion := "2.11.8"

resolvers ++= Seq(
  //"Pipeline Nexus Repository" at "http://rg-nexus:8080/nexus/content/repositories/nexus-rpm-global/",
  "spray repo" at "http://repo.spray.io",
  "MVN Repository" at "http://mvnrepository.com/",
  "Typesafe Repository" at "http://dl.bintray.com/typesafe/maven-releases/",
  "The New Motion Public Repo" at "http://nexus.thenewmotion.com/content/groups/public/",
  "Pipeline Nexus Repository" at "http://rg-nexus:8080/nexus/content/repositories/nexus-pipeline/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases",
  "Clicktale nexus global" at "http://rg-nexus/nexus/content/repositories/nexus-rpm-global"
)

libraryDependencies ++= Seq(
  //"org.scalatest" % "scalatest_2.11" % "2.2.0" % "test",
  //"info.cukes" % "cucumber-junit" % "1.1.8",
  //"com.novocode" % "junit-interface" % "0.10" % "test",
  //"log4j" % "log4j" % "1.2.14",
  //"org.slf4j" % "slf4j-log4j12" % "1.7.5",
  //"org.scala-lang" % "scala-library" % "2.11.1",
  "info.cukes" % "cucumber-scala_2.11" % "1.1.8",
  "info.cukes" % "cucumber-picocontainer" % "1.1.8",
  "junit" % "junit" % "4.11" % "test",
  "org.seleniumhq.selenium" % "selenium-java" % "2.42.2",
  "io.spray" %%  "spray-json" % "1.3.2",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "com.clicktale.pipeline" %% "common" % "[1.0.112,)",
  "com.aerospike" % "aerospike-client" % "latest.integration",
  "com.amazonaws" % "aws-java-sdk" % "1.11.256",
  "com.google.code.gson" % "gson" % "1.7.1",
  "com.mashape.unirest" % "unirest-java" % "1.3.0",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "com.github.nscala-time" %% "nscala-time" % "2.16.0",
  "org.apache.kafka" % "kafka-clients" % "0.10.2.0",
  "com.storm-enroute" %% "scalameter" % "0.7",
  "com.storm-enroute" %% "scalameter-core" % "0.7",
  "com.microsoft.azure" % "azure-storage" % "5.3.0",
  "com.clicktale" %% "recorder-events" % "3.2.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
)

libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5"
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.16.0"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.16"

//conflictManager := ConflictManager.strict

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")

mainClass in assembly := Some("com.clicktale.pipeline.regression.tools.PushSession")

test in assembly := {}

javaOptions += "-Djava.awt.headless=true"

// Publish artifact to nexus repo
credentials += Credentials("Sonatype Nexus Repository Manager", "rg-nexus", "deployment", "dep123")

// disable publishing the java doc jar

publishArtifact in(Compile, packageDoc) := false
publishMavenStyle := true
publishTo := {
  val nexus = "http://rg-nexus:8080/nexus/"
  Some("releases" at nexus + "content/repositories/nexus-pipeline")
}
