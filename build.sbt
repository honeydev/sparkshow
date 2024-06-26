ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
    .settings(
      name := "sparkshow"
    )

val http4sVersion = "0.23.22"
val doobieVersion = "1.0.0-RC2"
val pureConfigVersion = "0.17.4"
val logbackVersion = "1.2.3"
val log4CatsVersion = "2.5.0"
val izumiVersion = "1.2.5"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-core" % http4sVersion,
  "org.http4s" %% "http4s-client" % http4sVersion,
  "org.http4s" %% "http4s-server" % http4sVersion,
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  // Optional for auto-derivation of JSON codecs
  "io.circe" %% "circe-generic" % "0.14.5",
  // Optional for string interpolation to JSON model
  "io.circe" %% "circe-literal" % "0.14.5",
  "org.flywaydb" % "flyway-core" % "9.21.2",
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
//  "org.typelevel" %% "log4cats-slf4j" % log4CatsVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "io.7mind.izumi" %% "distage-core" % izumiVersion,
  "io.7mind.izumi" %% "distage-framework" % izumiVersion,
  "io.7mind.izumi" %% "distage-testkit-scalatest" % izumiVersion % Test,
  "org.scalameta" %% "munit" % "0.7.29" % Test,
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
  "org.mindrot" % "jbcrypt" % "0.4",
//  "com.lihaoyi" % "ammonite" % "3.0.0-M1" cross CrossVersion.full
)

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.17"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % Test
libraryDependencies += "org.scalamock" %% "scalamock" % "5.1.0" % Test
libraryDependencies += "com.github.scopt" %% "scopt" % "4.1.0"

addCompilerPlugin(
  "org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full
)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

scalacOptions ++= Seq("-Wunused")
