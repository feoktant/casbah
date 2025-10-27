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

import sbt.*

object Dependencies {
  private val specs2 = "3.8.9"
  private val slf = "1.6.0"

  val mongoJavaDriver  = "org.mongodb" % "mongo-java-driver" % "3.2.2"
  val slf4j            = "org.slf4j" % "slf4j-api" % slf
  val scalatime        = "com.github.nscala-time" %% "nscala-time" % "2.30.0"
  val scalaCompat      = "org.scala-lang.modules" %% "scala-collection-compat" % "2.14.0"

  val junit            = "junit" % "junit" % "4.10" % Test
  val slf4jJCL         = "org.slf4j" % "slf4j-jcl" % slf % Test

  val specs2Core  = "org.specs2" %% "specs2-core"  % specs2 % Test
  val specs2Junit = "org.specs2" %% "specs2-junit" % specs2 % Test
  val specs2Mock  = "org.specs2" %% "specs2-mock"  % specs2 % Test

  val scalatest = "org.scalatest" %% "scalatest" % "3.0.9"  % Test

}
