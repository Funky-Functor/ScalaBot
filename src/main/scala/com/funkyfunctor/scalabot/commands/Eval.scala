package com.funkyfunctor.scalabot.commands

import com.funkyfunctor.scalabot.Main.{ScalaBotContext, ScalabotEnvironment}
import com.funkyfunctor.scalabot.MessageReceivedException
import com.funkyfunctor.scalabot.utils.ZioUtils
import com.github.twitch4j.chat.TwitchChat
import zio.ZIO
import zio.logging.log

object EvalConstructor extends CommandConstructor {
  override val commandKey: String = "!eval"

  override def getCommand(commandArguments: String, context: CommandContext): ScalabotEnvironment[Command] = {
    for {
      chat    <- context.getElement[TwitchChat](CommandContext.CHAT_CLIENT_KEY)
      channel <- context.getElement[String](CommandContext.CHANNEL_KEY)
      _ <- log.debug(s"Constructing an EvalCommand with arguments - channel '$channel' - command '$commandArguments' ")
    } yield new EvalCommand(chat, channel, getCommandString(commandArguments))
  }.mapError(MessageReceivedException)
}

class EvalCommand(twitchChat: TwitchChat, channel: String, commandArguments: String) extends Command {
  override def run(): ZIO[ScalaBotContext, Nothing, Unit] = {
    val zio = log
      .debug(s"Received '$commandArguments' for my eval command")
      .as(twitchChat.sendMessage(channel, commandArguments))

    ZioUtils.processZio(zio, "Unexpected error while processing the eval command")
  }
}
