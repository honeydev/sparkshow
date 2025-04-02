ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
    .settings(
      name := "sparkshow"
    )

val http4sVersion       = "0.23.22"
val doobieVersion       = "1.0.0-RC3"
val pureConfigVersion   = "0.17.4"
val logbackVersion      = "1.3.5"
val log4CatsVersion     = "2.5.0"
val izumiVersion        = "1.2.5"
val circeVersion        = "0.14.6"
val circeGenericExtras  = "0.14.4"
val flywayVersion       = "10.15.2"
val doobieFlywayVersion = "0.4.0"
val jbcryptVersion      = "0.4"
val scalacticVersion    = "3.2.17"
val scoptVersion        = "4.1.0"
val munitVersion        = "0.7.29"
val scalatestVersion    = "3.2.17"
val scalamockVersion    = "5.1.0"
val jwtCircleVersion    = "10.0.1"
val sparkVersion        = "3.5.5"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-core" % http4sVersion,
  "org.http4s" %% "http4s-client" % http4sVersion,
  "org.http4s" %% "http4s-server" % http4sVersion,
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-literal" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeGenericExtras,
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat"  %% "doobie-postgres-circe" % doobieVersion,
//  "org.tpolecat" %% "doobie-specs2" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "de.lhns" %% "doobie-flyway" % doobieFlywayVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "io.7mind.izumi" %% "distage-core" % izumiVersion,
  "io.7mind.izumi" %% "distage-framework" % izumiVersion,
  "io.7mind.izumi" %% "distage-framework-docker" % izumiVersion,
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
  "org.mindrot" % "jbcrypt" % jbcryptVersion,
  "org.scalactic" %% "scalactic" % scalacticVersion,
  "com.github.scopt" %% "scopt" % scoptVersion,
  "com.github.jwt-scala" %% "jwt-circe" % jwtCircleVersion,
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.scalameta" %% "munit" % munitVersion % Test,
  "io.7mind.izumi" %% "distage-testkit-scalatest" % izumiVersion % Test,
  "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  "org.scalamock" %% "scalamock" % scalamockVersion % Test
)


addCompilerPlugin(
  "org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full
)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

scalacOptions ++= Seq("-Wunused", "-target:jvm-17", "-Ymacro-annotations")

inThisBuild(
  List(
    scalaVersion := "2.13.13",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)
