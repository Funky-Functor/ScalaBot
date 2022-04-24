package com.funkyfunctor.scalabot.commands

import com.funkyfunctor.scalabot.Main.{ScalaBotContext, ScalabotEnvironment}
import com.funkyfunctor.scalabot.MessageReceivedException
import com.funkyfunctor.scalabot.utils.ZioUtils
import com.github.twitch4j.chat.TwitchChat
import zio.ZIO

object PingConstructor extends CommandConstructor {
  override val commandKey: String = "ff_ping"

  override def getCommand(command: String, context: CommandContext): ScalabotEnvironment[Command] = {
    for {
      chat    <- context.getElement[TwitchChat](CommandContext.CHAT_CLIENT_KEY)
      channel <- context.getElement[String](CommandContext.CHANNEL_KEY)
    } yield new PingCommand(chat, channel)
  }.mapError(MessageReceivedException)
}

class PingCommand(twitchChat: TwitchChat, channel: String) extends Command {
  override def run(): ZIO[ScalaBotContext, Nothing, Unit] = {
    val zio = ZIO(twitchChat.sendMessage(channel, "PONG !!!"))
    ZioUtils.processZio(zio, "Unexpected error while processing the ping command")
  }
}
