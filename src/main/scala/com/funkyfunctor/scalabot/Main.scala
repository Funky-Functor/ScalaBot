package com.funkyfunctor.scalabot

import com.funkyfunctor.scalabot.Configuration.HasConfiguration
import com.funkyfunctor.scalabot.CustomLayer.{DefaultCustomLayer, ScalaBotSpecificContext}
import com.funkyfunctor.scalabot.eventHandlers.{EventHandler, MessageReceivedHandler}
import zio._
import zio.console.putStrLn
import zio.logging._

object Main extends App {
  type ScalaBotContext         = ScalaBotSpecificContext with ZEnv
  type ScalabotEnvironment[A]  = ZIO[ScalaBotContext, ScalaBotException, A]

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {

    startBot()
      .fold(
        _ => putStrLn("ERROR"),
        _ => putStrLn("SUCCESS")
      )
      .provideCustomLayer(DefaultCustomLayer.fullCustomLayer)
      .exitCode
  }

  def startBot(): ScalabotEnvironment[Unit] = {
    for {
      client <- ScalabotTwitchClient.createTwitchClient()
      _      <- ScalabotTwitchClient.joinDefaultChannel(client)
      _ <- EventHandler.registerHandlers(client, Seq(MessageReceivedHandler.eventHandler))
      _ <- ZIO.accessM { (hasConf: HasConfiguration) =>
        log.info(s"My configuration is ${hasConf.get}")
      }
    } yield ()
  }
}
