import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}

val now = {
  //Gives me the time in the format yyyyMMdd-hhmmss
  val date      = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))
  val formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")

  formatter.format(date)
}

/*General config */
ThisBuild / scalaVersion     := Versions.scalaVersion
ThisBuild / version          := s"0.1.0-$now"
ThisBuild / organization     := "com.funkyfunctor.scalabot"
ThisBuild / organizationName := "Funky Functor Inc."

/*Docker-specific config */
enablePlugins(JavaAppPackaging, DockerPlugin)
Docker / maintainer := "chris@funky-functor.com"
dockerBaseImage     := "arm64v8/openjdk:17-jdk-slim-buster" //Used for Raspberry Pi deployment - Use "openjdk:17-jdk-alpine" for regular Docker deployment
dockerUpdateLatest  := true

/* Scala compiler config */
scalacOptions ++= Seq(
  "-encoding", "utf8",
  //"-Xfatal-warnings",
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps",

  //Scala 3 specific
//  "-source:3.0-migration",
//  "-rewrite",
//  "-indent",
//  "-new-syntax"
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

/* Project-specific config */
lazy val root = (project in file("."))
  .settings(
    name := "ScalaBot",
    libraryDependencies ++= Seq(
      Libraries.zio,
      Libraries.twitch4j,
      Libraries.typesafeConfiguration,
      Libraries.zioLogging,
      Libraries.scalaReflection,
      Libraries.fastParse,

      //Tests
      Libraries.zioTest,
      Libraries.zioTestSbt
    )
  )
