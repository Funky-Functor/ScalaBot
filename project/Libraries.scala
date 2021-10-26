import sbt._

object Versions {
  val scalaVersion = "2.13.6"

  val zioVersion                   = "1.0.12"
  val twitch4jVersion              = "1.5.0"
  val typesafeConfigurationVersion = "1.4.1"
  val zioLoggingVersion            = "0.5.13"

}

object Libraries {
  import Versions._

  val zio                   = "dev.zio"            %% "zio"           % zioVersion
  val zioTest               = "dev.zio"            %% "zio-test"      % zioVersion % Test
  val scalaReflection       = "org.scala-lang"      % "scala-reflect" % scalaVersion
  val twitch4j              = "com.github.twitch4j" % "twitch4j"      % twitch4jVersion
  val typesafeConfiguration = "com.typesafe"        % "config"        % typesafeConfigurationVersion
  val zioLogging            = "dev.zio"            %% "zio-logging"   % zioLoggingVersion
}
