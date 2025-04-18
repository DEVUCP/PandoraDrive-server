ThisBuild / scalaVersion := "3.3.5"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"
lazy val root = (project in file("."))
  .settings(
    name := "PandoraHomeDrive",
    libraryDependencies ++= Seq(
      // HTTP4S (latest stable)
      "org.http4s" %% "http4s-ember-server" % "0.23.26",
      "org.http4s" %% "http4s-ember-client" % "0.23.26",
      "org.http4s" %% "http4s-dsl" % "0.23.26",
      "org.http4s" %% "http4s-circe" % "0.23.26",

      // Circe (Scala 3 compatible versions)
      "io.circe" %% "circe-generic" % "0.14.6",
      "io.circe" %% "circe-parser" % "0.14.6",

      // Logging
      "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
      "ch.qos.logback" % "logback-classic" % "1.4.14",

      // Testing
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
      "org.scalatest" %% "scalatest" % "3.2.18" % Test,

      // Start with this one
      "org.xerial" % "sqlite-jdbc" % "3.23.1",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC8",

      // And add any of these as needed
      "org.tpolecat" %% "doobie-h2" % "1.0.0-RC8", // H2 driver 1.4.200 + type mappings.
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC8",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC8", // HikariCP transactor.
      "org.tpolecat" %% "doobie-specs2" % "1.0.0-RC8" % "test", // Specs2 support for typechecking statements.
      "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC8" % "test" // ScalaTest support for typechecking statements.
    )
  )
