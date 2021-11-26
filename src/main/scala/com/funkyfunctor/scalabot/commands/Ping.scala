package com.funkyfunctor.scalabot.commands

import com.github.twitch4j.chat.TwitchChat

import scala.util.Try

object PingConstructor extends CommandConstructor {
  override def commandKey: String = "!ff_ping"

  override def getCommand(commandArguments: Seq[String], context: Map[String, Object]): Option[Command] =
    for {
      chatObject <- context.get(CommandContext.CHAT_CLIENT_KEY)
      chat <- Try {
        chatObject.asInstanceOf[TwitchChat]
      }.toOption
      channelObject <- context.get(CommandContext.CHANNEL_KEY)
      channel = channelObject.toString
    } yield new PingCommand(chat, channel)
}

class PingCommand(twitchChat: TwitchChat, channel: String) extends Command {
  override def run(): Unit = twitchChat.sendMessage(channel, "PONG !!!")
}
