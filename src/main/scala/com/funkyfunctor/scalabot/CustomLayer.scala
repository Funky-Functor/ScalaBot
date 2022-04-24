package com.funkyfunctor.scalabot

import com.funkyfunctor.scalabot.Configuration.HasConfiguration
import com.funkyfunctor.scalabot.CustomLayer.ScalaBotSpecificContext
import zio.logging.{LogFormat, LogLevel, Logging}
import zio.{ZEnv, ZLayer}

object CustomLayer {
  type ScalaBotSpecificContext = HasConfiguration with Logging

  case object DefaultCustomLayer extends CustomLayer {
    // Dependency injection
    val configuration: ZLayer[ZEnv, ScalaBotException, HasConfiguration] = Configuration.retrieveConfiguration()
    val logging: ZLayer[ZEnv, Nothing, Logging] = Logging.console(
      logLevel = LogLevel.Debug,
      format = LogFormat.ColoredLogFormat()
    ) >>> Logging.withRootLoggerName("com.funkyfunctor.scalabot")
  }
}

trait CustomLayer {
  def configuration: ZLayer[ZEnv, ScalaBotException, HasConfiguration]

  val logging: ZLayer[ZEnv, Nothing, Logging]

  def fullCustomLayer: ZLayer[zio.ZEnv, ScalaBotException, ScalaBotSpecificContext] = configuration ++ logging
}
