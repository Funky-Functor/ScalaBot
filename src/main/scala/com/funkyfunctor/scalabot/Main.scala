package com.funkyfunctor.scalabot

import com.funkyfunctor.scalabot.Configuration.HasConfiguration
import com.funkyfunctor.scalabot.eventHandlers.{EventHandler, MessageReceivedHandler}
import zio.*
import zio.logging.*

object Main extends App:
  type ScalabotEnvironment[A] = ZIO[HasConfiguration with Logging, ScalaBotException, A]

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    // Dependency injection
    val configuration: ZLayer[ZEnv, ScalaBotException, HasConfiguration] = Configuration.retrieveConfiguration()
    val logging: ZLayer[ZEnv, Nothing, Logging] = Logging.console(
      logLevel = LogLevel.Info,
      format = LogFormat.ColoredLogFormat()
    ) >>> Logging.withRootLoggerName("com.funkyfunctor.scalabot")

    startBot()
      .provideCustomLayer(configuration ++ logging)
      .fold(
        _ => System.out.println("ERROR"),
        _ => System.out.println("SUCCESS")
      )
      .exitCode
  end run

  def startBot(): ScalabotEnvironment[Unit] =
    for
      client <- ScalabotTwitchClient.createTwitchClient()
      _      <- ScalabotTwitchClient.joinDefaultChannel(client)
      _      <- EventHandler.registerHandlers(client, Seq(MessageReceivedHandler))
      _ <- ZIO.accessM { (hasConf: HasConfiguration) =>
        log.info(s"My configuration is ${hasConf.get}")
      }
    yield ()
  end startBot
end Main
