package com.funkyfunctor.scalabot.twitch

import com.funkyfunctor.scalabot.twitch.Configuration.HasConfiguration
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.{TwitchClient, TwitchClientBuilder}
import zio.ZIO

object ScalabotTwitchClient {
  def createTwitchClient(): ZIO[HasConfiguration, ScalaBotException, TwitchClient] = {
    ZIO.accessM { hasConf =>
      val conf = hasConf.get[Configuration]

      ZIO {
        val credentials = new OAuth2Credential(
          "twitch",
          conf.irc
        );

        val clientBuilder = TwitchClientBuilder.builder();

        clientBuilder
          .withClientId(conf.twitchClientId)
          .withClientSecret(conf.twitchClientSecret)
          .withEnableHelix(true)
          /*
           * Chat Module
           * Joins irc and triggers all chat based events (viewer join/leave/sub/bits/gifted subs/...)
           */
          .withChatAccount(credentials)
          .withEnableChat(true)
          /*
           * GraphQL has a limited support
           * Don't expect a bunch of features enabling it
           */
          //.withEnableGraphQL(true)
          /*
           * Kraken is going to be deprecated
           * see : https://dev.twitch.tv/docs/v5/#which-api-version-can-you-use
           * It is only here so you can call methods that are not (yet)
           * implemented in Helix
           */
          .withEnableKraken(true)
          /*
           * Build the TwitchClient Instance
           */
          .build();
      }.mapError(throwable => ConfigurationLoadingException(throwable))
    }
  }

  def sendMessage(client: TwitchClient, message: String, channel: String): ZIO[HasConfiguration, ScalaBotException, Boolean] = ZIO{
    client.getChat.sendMessage(channel, message)
  }.mapError(error => ChatMessageSendingException(channel, error))
}
