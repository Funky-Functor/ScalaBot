package com.funkyfunctor.scalabot

import com.funkyfunctor.scalabot.Configuration.HasConfiguration
import com.funkyfunctor.scalabot.eventHandlers.{EventHandler, MessageReceivedHandler}
import zio._
import zio.logging._

object Main extends App {
  type ScalabotEnvironment[A] = ZIO[HasConfiguration with Logging, ScalaBotException, A]

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    //Dependency injection
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
  }

  def startBot(): ScalabotEnvironment[Unit] = {
    for {
      client <- ScalabotTwitchClient.createTwitchClient()
      _      <- ScalabotTwitchClient.joinDefaultChannel(client)
      _      <- EventHandler.registerHandlers(client, Seq(MessageReceivedHandler))
      _ <- ZIO.accessM { hasConf: HasConfiguration =>
        log.info(s"My configuration is ${hasConf.get}")
      }
    } yield ()
  }
//
//  lazy val demoEventHandler: EventHandler[DonationEvent] = new EventHandler[DonationEvent]({ event: DonationEvent =>
//    val user        = event.getUser.getName
//    val amount      = event.getAmount
//    val source      = event.getSource
//    val channelName = event.getChannel.getName
//
//    val message = s"$user just donated $amount using $source!"
//
//    event.getTwitchChat.sendMessage(channelName, message)
//  })
//
//  lazy val otherDemoEventHandler: EventHandler[FollowEvent] = new EventHandler[FollowEvent]({ event =>
//    val user        = event.getUser.getName
//    val channelName = event.getChannel.getName
//
//    val message = s"$user is now following $channelName!"
//
//    event.getTwitchChat.sendMessage(channelName, message);
//  })
}
