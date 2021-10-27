ThisBuild / scalaVersion     := Versions.scalaVersion
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.funkyfunctor.scalabot"
ThisBuild / organizationName := "Funky Functor Inc."

lazy val root = (project in file("."))
  .settings(
    name := "ScalaBot",
    libraryDependencies ++= Seq(
      Libraries.zio,
      Libraries.twitch4j,
      Libraries.typesafeConfiguration,
      Libraries.zioLogging,
      Libraries.scalaReflection,

      //Java
      Libraries.lombok,

      //Tests
      Libraries.zioTest
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
