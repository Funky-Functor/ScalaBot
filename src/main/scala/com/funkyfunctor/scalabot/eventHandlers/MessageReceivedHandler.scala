package com.funkyfunctor.scalabot.eventHandlers

import com.funkyfunctor.scalabot.Main.{ScalaBotContext, ScalabotEnvironment}
import com.funkyfunctor.scalabot.MessageReceivedException
import com.funkyfunctor.scalabot.commands.{Command, CommandContext}
import com.funkyfunctor.scalabot.utils.ZioUtils
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import zio._
import zio.logging._

import scala.jdk.javaapi.CollectionConverters

object MessageReceivedHandler {
  def eventHandler: ZioEventHandler[ChannelMessageEvent] = {
    val effect: ChannelMessageEvent => URIO[ScalaBotContext, Unit] = { (event: ChannelMessageEvent) =>
      {
        val result = for {
          msg        <- getMessageInformation(event)
          msgContext <- getMessageContext(event)
          command    <- Command.toCommand(msg, msgContext)
          result     <- command.run()
        } yield result

        ZioUtils.processZio(result, "An error was encountered while processing a ChannelMessageEvent")
      }
    }
    new ZioEventHandler[ChannelMessageEvent](effect)
  }

  def getMessageInformation(event: ChannelMessageEvent): ScalabotEnvironment[String] = {
    for {
      permissions <- ZIO(CollectionConverters.asScala(event.getPermissions))
      user        <- ZIO(event.getUser.getName)
      msg         <- ZIO(event.getMessage)
      timestamp   <- clock.nanoTime
      _           <- log.info(s"$timestamp - [$user ($permissions) says] '$msg'")
    } yield msg
  }.mapError(MessageReceivedException)

  def getMessageContext(event: ChannelMessageEvent): ScalabotEnvironment[Map[String, Object]] = {
    for {
      channelName <- ZIO(event.getChannel.getName)
      chatClient  <- ZIO(event.getTwitchChat)
    } yield Map(
      CommandContext.CHANNEL_KEY     -> channelName,
      CommandContext.CHAT_CLIENT_KEY -> chatClient
    )
  }.mapError(MessageReceivedException)
}
