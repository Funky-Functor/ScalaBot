package com.funkyfunctor.scalabot

import com.typesafe.config.ConfigFactory
import zio._

object Configuration {
  type HasConfiguration = Has[Configuration]

  val IRC_KEY                    = "irc"
  val TWITCH_CLIENT_CATEGORY_KEY = "twitchClient"
  val TWITCH_CLIENT_ID_KEY       = "id"
  val TWITCH_CLIENT_SECRET_KEY   = "secret"

  def retrieveConfiguration(): ZLayer[Any, ScalaBotException, HasConfiguration] = {
    ZIO {
      val conf = ConfigFactory.load()

      val irc                = conf.getString(IRC_KEY)
      val twitchClientConfig = conf.getConfig(TWITCH_CLIENT_CATEGORY_KEY)

      val twitchClientId     = twitchClientConfig.getString(TWITCH_CLIENT_ID_KEY)
      val twitchClientSecret = twitchClientConfig.getString(TWITCH_CLIENT_SECRET_KEY)

      Configuration(twitchClientId = twitchClientId, twitchClientSecret = twitchClientSecret, irc = irc)
    }
      .mapError(error => ConfigurationLoadingException(error))
      .toLayer
  }
}

case class Configuration private (twitchClientId: String, twitchClientSecret: String, irc: String) {}
