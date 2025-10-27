/*
 * Copyright 2015 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//import com.typesafe.sbt.SbtScalariform.{ScalariformKeys, _}
//import org.scalastyle.sbt.ScalastylePlugin._
import sbt.Keys._
import sbt._
//import sbtunidoc.Plugin._

// Global build settings
ThisBuild / organization := "org.mongodb"
ThisBuild / organizationHomepage := Some(url("http://www.mongodb.org"))
ThisBuild / version := "3.1.2-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.18"
ThisBuild / crossScalaVersions := Seq("2.12.18")
ThisBuild / javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

// Resolvers
ThisBuild / resolvers ++= Seq(
  "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
  "TypeSafe snapshots" at "https://repo.typesafe.com/typesafe/snapshots",
  "TypeSafe releases" at "https://repo.typesafe.com/typesafe/releases",
)

// ScalacOptions settings
val scalacOptionsSettings = Seq(scalacOptions ++= (scalaBinaryVersion.value match {
  case "2.10" => Seq("-unchecked", "-feature", "-Xlint")
  case _ => Seq("-unchecked", "-feature", "-Xlint:-missing-interpolator")
}))

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
    "org.mongodb" % "mongo-java-driver" % "3.2.2",
    "org.slf4j" % "slf4j-api" % "1.6.0",
    "com.github.nscala-time" %% "nscala-time" % "2.14.0",
    "org.slf4j" % "slf4j-jcl" % "1.6.0" % Test,
    "org.scalatest" %% "scalatest" % "3.0.8" % Test,
    "org.specs2" %% "specs2-core"  % "3.8.6" % Test,
    "org.specs2" %% "specs2-junit" % "3.8.6" % Test,
    "org.specs2" %% "specs2-mock"  % "3.8.6" % Test
  )
)

// Common settings for all projects
val casbahDefaultSettings = scalacOptionsSettings ++
  testSettings ++
  //  customScalariformSettings ++
  //  scalaStyleSettings ++
  casbahCommonDependencies ++
  Seq(
    // Exclude potential problematic files during sbt 1.x migration
    Compile / excludeFilter := (Compile / excludeFilter).value || "**/legacy/**" || "**/*Deprecated*",
    Test / excludeFilter := (Test / excludeFilter).value || "**/legacy/**" || "**/*Deprecated*"
  )

// Check style alias
val checkAlias = addCommandAlias("check", ";clean;scalastyle;coverage;test;coverageAggregate;coverageReport")

//// Root unidoc settings
//val rootUnidocSettings = Seq(
//  Compile / doc / scalacOptions ++= Opts.doc.title("Casbah Driver"),
//  Compile / doc / scalacOptions ++= Seq("-diagrams", "-unchecked", "-doc-root-content", "rootdoc.txt")
//) ++ unidocSettings

// Define subprojects
lazy val commons = project.in(file("casbah-commons"))
  .settings(casbahDefaultSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.mongodb" % "mongodb-driver-sync" % "4.3.4",
      "org.slf4j" % "slf4j-api" % "1.7.30",
      "org.slf4j" % "slf4j-jcl" % "1.7.30"
    ),
    Test / publishArtifact := true
  )

lazy val core = project.in(file("casbah-core"))
  .dependsOn(commons % "test->test;compile")
  .dependsOn(query)
  .settings(casbahDefaultSettings)
  .settings(
    Test / parallelExecution := false
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

// Define the root project to aggregate all subprojects
lazy val casbah = (project in file("."))
  .aggregate(commons, core, query, gridfs)
  .dependsOn(commons, core, query, gridfs)
  .settings(casbahDefaultSettings)
  //  .settings(rootUnidocSettings)
  .settings(
    publish / skip := false,
    initialCommands / console := """import com.mongodb.casbah.Imports._"""
  )
  .settings(checkAlias)
