name := "DumbDoctor"

version := "0.1"

scalaVersion := "2.12.8"

resolvers ++= Seq(
  "Maven Artifacts" at "https://search.maven.org/artifact",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
)


libraryDependencies ++= Seq(
  "net.ruippeixotog" %% "scala-scraper" % "2.1.0",
  "eu.fakod" % "neo4j-scala_2.11" % "0.3.4-SNAPSHOT",
  "com.dimafeng" %% "neotypes" % "0.4.0",
  "com.typesafe" % "config" % "1.3.2"
)