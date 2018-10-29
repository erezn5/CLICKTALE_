organization := "com.clicktale.pipe"

name := "pipe-diff"

version := "1.0.0.11"

scalaVersion := "2.12.6"

resolvers ++= Seq(
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.ivy2/local",
  "nexus-maven-virtual" at "http://rg-nexus:8080/nexus/content/repositories/nexus-maven-virtual/",
  "Pipeline Nexus Repository" at "http://rg-nexus:8080/nexus/content/repositories/nexus-pipeline/"
)

val akkaVersion = "2.5.14"

libraryDependencies ++= Seq(
  "com.clicktale.pipe" %% "util" % "2.9.0.7",
  "com.typesafe" % "config" % "1.3.3",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.22",
  "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % "0.20",
  "com.typesafe.akka" %% "akka-http" % "10.1.3",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion, // or whatever the latest version is,
  //  "com.github.blemale" %% "scaffeine" % "2.5.0", // cache library for kafka solution
  "org.gnieh" %% "diffson-circe" % "3.0.0",
  "com.flipkart.zjsonpatch" % "zjsonpatch" % "0.4.4",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

// begin - circe dependency
val circeVersion = "0.9.3"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
)
// end - circe dependency

//testOptions in Test += Tests.Argument("-oF")

// rules for assembly pluging
val mainCls = "com.clicktale.pipe.diff.PipeDiff"
mainClass in Compile := Some(mainCls)
mainClass in assembly := Some(mainCls)
mainClass in packageBin := Some(mainCls)

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", _*) | PathList("javax", "annotation", _*) => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}


// Packaging using sbt-native-packager
// -----------------------------------

enablePlugins(JavaServerAppPackaging, RpmPlugin, RpmDeployPlugin, UpstartPlugin)
packageName in Rpm := s"${name.value}"
packageArchitecture in Rpm := "noarch"
assemblyJarName := s"${name.value}_${version.value}.jar"
scriptClasspath := Seq(assemblyJarName.value)

serviceAutostart := false //--
daemonUser in Linux := "pipe-diff"
daemonGroup in Linux := "pipe-diff"

defaultLinuxInstallLocation := "/opt/clicktale"
defaultLinuxLogsLocation := "/var/log"
rpmPrefix := Some(defaultLinuxInstallLocation.value)

linuxPackageSymlinks ~= {
  _.filterNot(_.link == "boot").filterNot(_.destination == "boot")
}

mappings in Universal ~= {
  _.filterNot(_._2.endsWith(".jar"))
}
mappings in Universal ++= Seq(
  (resourceDirectory in Compile).value / "application.conf" -> "conf/application.conf",
  (resourceDirectory in Compile).value / "logback.xml" -> "conf/logback.xml",
  (assembly in Compile).value -> ("lib/" + (assembly in Compile).value.getName))

// https://github.com/sbt/sbt-native-packager/issues/945
linuxPackageMappings += packageTemplateMapping(s"/var/lib/${name.value}")() withUser name.value withGroup name.value

// Pass the location of the log + app configuration files via javaOptions
javaOptions in Universal ++= Seq(
  s"-Dconfig.file=/etc/${name.value}/application.conf",
  s"-Dlogback.configurationFile=/etc/${name.value}/logback.xml",
  "-XX:-OmitStackTraceInFastThrow"
)

rpmVendor := "Clicktale"
rpmLicense := Some("Clicktale")
packageSummary in Rpm := "Clicktale PipeDiff Service"
packageDescription in Linux := "Operate diff of 2 processor base on their output files requested from Cage. "
maintainer in Linux := "Pipeline Team"

credentials += Credentials("Sonatype Nexus Repository Manager", "rg-nexus", "deployment", "dep123")
publishArtifact in(Compile, packageDoc) := false
publishArtifact in(Compile, packageSrc) := false
publishTo := {
  val nexus = "http://rg-nexus:8080/nexus/"
  Some("releases" at nexus + "content/repositories/nexus-rpm/")
}
