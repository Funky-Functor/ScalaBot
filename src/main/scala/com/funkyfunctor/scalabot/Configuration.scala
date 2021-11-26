package com.funkyfunctor.scalabot

import com.typesafe.config.ConfigFactory
import zio._

object Configuration {
  type HasConfiguration = Has[Configuration]

  val IRC_KEY                       = "irc"
  val TWITCH_CLIENT_CATEGORY_KEY    = "twitchClient"
  val TWITCH_CLIENT_ID_KEY          = "id"
  val TWITCH_CLIENT_SECRET_KEY      = "secret"
  val TWITCH_CLIENT_DEFAULT_CHANNEL = "channel"

  def retrieveConfiguration(): ZLayer[Any, ScalaBotException, HasConfiguration] = {
    ZIO {
      val conf = ConfigFactory.load()

      val twitchClientConfig = conf.getConfig(TWITCH_CLIENT_CATEGORY_KEY)
      val irc                = twitchClientConfig.getString(IRC_KEY)
      val twitchClientId     = twitchClientConfig.getString(TWITCH_CLIENT_ID_KEY)
      val twitchClientSecret = twitchClientConfig.getString(TWITCH_CLIENT_SECRET_KEY)
      val channel            = twitchClientConfig.getString(TWITCH_CLIENT_DEFAULT_CHANNEL)

      Configuration(
        twitchClientId = twitchClientId,
        twitchClientSecret = twitchClientSecret,
        irc = irc,
        defaultChannel = channel
      )
    }
      .mapError(error => ConfigurationLoadingException(error))
      .toLayer
  }

  def partlyHide(str: String, charactersKeptAtTheEnd: Int = 3): String = {
    val strSize = str.length;

    if (strSize <= charactersKeptAtTheEnd) str
    else {
      val limit = strSize - charactersKeptAtTheEnd
      val begin = str.substring(0, limit).replaceAll(".", "X")
      val end   = str.substring(limit)

      begin + end
    }
  }
}

case class Configuration private (
    twitchClientId: String,
    twitchClientSecret: String,
    irc: String,
    defaultChannel: String
) {
  override def toString: String =
    s"""Configuration(
      | twitchClientId:     ${Configuration.partlyHide(twitchClientId)}
      | twitchClientSecret: ${Configuration.partlyHide(twitchClientSecret)}
      | irc:                ${Configuration.partlyHide(irc)}
      | defaultChannel:     $defaultChannel
      |)""".stripMargin
}
