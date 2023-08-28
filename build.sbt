ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "sparkshow"
  )

val http4sVersion = "0.23.22"
val doobieVersion = "1.0.0-RC2"
val pureConfigVersion = "0.17.4"
val logbackVersion = "1.2.11"
val log4CatsVersion = "2.5.0"

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
  "org.typelevel" %% "log4cats-slf4j" % log4CatsVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.11",
  "org.scalameta" %% "munit" % "0.7.29" % Test,
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")