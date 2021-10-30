package com.funkyfunctor.scalabot.twitch

import com.funkyfunctor.scalabot.twitch.Configuration.HasConfiguration
import com.funkyfunctor.scalabot.twitch.eventHandlers.{DonationEventHandler, EventHandler, FollowEventHandler}
import zio.logging.{LogFormat, LogLevel, Logging, log}
import zio.{App, ExitCode, URIO, ZEnv, ZIO, ZLayer}

object Main extends App {
  type ScalabotEnvironment[A] = ZIO[HasConfiguration with Logging, ScalaBotException, A]

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    //Dependency injection
    val configuration: ZLayer[ZEnv, ScalaBotException, HasConfiguration] = Configuration.retrieveConfiguration()
    val logging: ZLayer[ZEnv, Nothing, Logging] = Logging.console(
      logLevel = LogLevel.Debug,
      format = LogFormat.ColoredLogFormat()
    ) >>> Logging.withRootLoggerName("com.funkyfunctor.scalabot")

    (startBot() *> {
      ZIO.accessM { hasConf: HasConfiguration =>
        log.info(s"My configuration is ${hasConf.get}")
      }
    })
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
      _      <- EventHandler.registerHandlers(client, Seq(donationEventHandler, followEventHandler))
      _      <- ScalabotTwitchClient.sendMessage(client, "Hello @grimli! This is the Funky Scala Bot!", "grimli")
    } yield ()
  }

  val followEventHandler: FollowEventHandler = FollowEventHandler({ event =>
    val user        = event.getUser.getName
    val channelName = event.getChannel.getName

    val message = s"$user is now following $channelName!"

    event.getTwitchChat.sendMessage(channelName, message);
  })

  val donationEventHandler: DonationEventHandler = DonationEventHandler({ event =>
    val user        = event.getUser.getName
    val amount      = event.getAmount
    val source      = event.getSource
    val channelName = event.getChannel.getName

    val message = s"$user just donated $amount using $source!"

    event.getTwitchChat.sendMessage(channelName, message)
  })
}
