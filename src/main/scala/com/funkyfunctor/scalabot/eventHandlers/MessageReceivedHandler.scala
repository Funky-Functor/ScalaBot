package com.funkyfunctor.scalabot.eventHandlers

import com.funkyfunctor.scalabot.commands.{Command, CommandContext}
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent

import scala.jdk.javaapi.CollectionConverters

object MessageReceivedHandler
    extends EventHandler[ChannelMessageEvent]({ event =>
      val permissions = CollectionConverters.asScala(event.getPermissions)
      //event.
      val user      = event.getUser.getName
      val msg       = event.getMessage
      val timestamp = System.currentTimeMillis()

      System.out.println(s"$timestamp - [$user ($permissions) says] '$msg'")

      val commandContext = Map(
        CommandContext.CHANNEL_KEY -> event.getChannel.getName,
        CommandContext.CHAT_CLIENT_KEY -> event.getTwitchChat
      )

      Command.toCommand(msg, commandContext).foreach(
        command => command.run()
      )
    })
