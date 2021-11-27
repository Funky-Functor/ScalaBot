package com.funkyfunctor.scalabot.commands

object CommandContext {
  val CHAT_CLIENT_KEY = "TwitchChat"
  val CHANNEL_KEY     = "channel"
}

case class CommandContext(
    envVariables: Map[String, Any],
    userVariables: Map[String, Any],
    date: Long
)
