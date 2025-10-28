//import com.typesafe.sbt.SbtScalariform.{ScalariformKeys, _}
//import org.scalastyle.sbt.ScalastylePlugin._
import sbt.Keys.*
import sbt.*
//import sbtunidoc.Plugin._

// Global build settings
ThisBuild / organization := "io.feoktant"
ThisBuild / organizationHomepage := Some(url("http://www.mongodb.org"))
ThisBuild / licenses := List(
  "Apache 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"),
)
ThisBuild / version := "3.1.2-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.18"
ThisBuild / crossScalaVersions := Seq("2.12.18")
ThisBuild / javacOptions ++= Seq("-source", "21", "-target", "21", "-Xlint")

ThisBuild / resolvers ++= Seq(
  "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
  "TypeSafe snapshots" at "https://repo.typesafe.com/typesafe/snapshots",
  "TypeSafe releases" at "https://repo.typesafe.com/typesafe/releases",
)

inThisBuild(List(
  semanticdbEnabled := true,
  semanticdbOptions += "-P:semanticdb:synthetics:on",
  semanticdbVersion := scalafixSemanticdb.revision,
  scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value),
))

val isCi = settingKey[Boolean]("Detects if the build is running on a CI environment.")

val scalacOptionsSettings = Seq(
  scalacOptions ++= Seq("-unchecked", "-feature", "-Xlint:-missing-interpolator")
)

// Test Settings
val testSettings = Seq(
  testFrameworks += TestFrameworks.Specs2,
  Test / parallelExecution := true
)

//// Style and formatting
//def scalariFormFormattingPreferences = {
//  import scalariform.formatter.preferences._
//  FormattingPreferences()
//    .setPreference(AlignParameters, true)
//    .setPreference(AlignSingleLineCaseStatements, true)
//    .setPreference(DoubleIndentClassDeclaration, true)
//}

//val customScalariformSettings = scalariformSettings ++ Seq(
//  ScalariformKeys.preferences in Compile := scalariFormFormattingPreferences,
//  ScalariformKeys.preferences in Test := scalariFormFormattingPreferences
//)
//
//val scalaStyleSettings = Seq(
//  (Compile / scalastyleConfig) := file("project/scalastyle-config.xml"),
//  (Test / scalastyleConfig) := file("project/scalastyle-config.xml")
//)

// Common dependencies
val casbahCommonDependencies = Seq(
  libraryDependencies ++= Seq(
    Dependencies.mongoJavaDriver,
    Dependencies.slf4j,
    Dependencies.scalatime,
    Dependencies.slf4jJCL,
    Dependencies.scalatest,
    Dependencies.specs2Core,
    Dependencies.specs2Junit,
    Dependencies.specs2Mock,
  ) ++ {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) =>
        Seq("org.scala-lang.modules" %% "scala-collection-compat" % "2.14.0")
      case _ => Nil
    }
  },
)

// Common settings for all projects
val casbahDefaultSettings = scalacOptionsSettings ++
  testSettings ++
  //  customScalariformSettings ++
  //  scalaStyleSettings ++
  casbahCommonDependencies

// Check style alias
val checkAlias = addCommandAlias("check", ";clean;scalastyle;coverage;test;coverageAggregate;coverageReport")

//// Root unidoc settings
//val rootUnidocSettings = Seq(
//  Compile / doc / scalacOptions ++= Opts.doc.title("Casbah Driver"),
//  Compile / doc / scalacOptions ++= Seq("-diagrams", "-unchecked", "-doc-root-content", "rootdoc.txt")
//) ++ unidocSettings

lazy val commons = project.in(file("casbah-commons"))
  .settings(casbahDefaultSettings)
  .settings(
    Test / publishArtifact := true
  )

lazy val core = project.in(file("casbah-core"))
  .dependsOn(commons % "test->test;compile")
  .dependsOn(query)
  .settings(casbahDefaultSettings)
  .settings(
    Test / parallelExecution := false,

    // Explicitly stop MaxTimeSpec test execution on GithubActions
    // since there is no way to `--setParameter enableTestCommands=1` on running mongo container.
    // TODO: find the way how to fix this
    Test / testOptions += Tests.Filter { testClass =>
      !(testClass.endsWith("MaxTimeSpec") && sys.env.get("CI").contains("true"))
    },
  )

lazy val query = project.in(file("casbah-query"))
  .dependsOn(commons % "test->test;compile")
  .settings(casbahDefaultSettings)

lazy val gridfs = project.in(file("casbah-gridfs"))
  .dependsOn(commons % "test->test", core % "test->test;compile")
  .settings(casbahDefaultSettings)

lazy val examples = project.in(file("examples"))
  .dependsOn(casbah)
  .settings(casbahDefaultSettings)
  .settings(
    publish / skip := true
  )

lazy val casbah = (project in file("."))
  .aggregate(commons, core, query, gridfs)
  .dependsOn(commons, core, query, gridfs)
  .enablePlugins(ScalaUnidocPlugin)
  .settings(casbahDefaultSettings)
  .settings(
    publish / skip := false,
    initialCommands / console := """import com.mongodb.casbah.Imports._"""
  )
  .settings(checkAlias)
