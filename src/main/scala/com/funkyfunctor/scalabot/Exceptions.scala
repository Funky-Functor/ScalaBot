package com.funkyfunctor.scalabot

sealed abstract class ScalaBotException(errorMessage: String, innerException: Throwable)
    extends Exception(errorMessage, innerException)

case class ConfigurationLoadingException(innerException: Throwable)
    extends ScalaBotException("An error has occurred while trying to read the configuration", innerException)
case class TwitchClientLoadingException(innerException: Throwable)
    extends ScalaBotException("An error has occurred while trying to create a Twitch client", innerException)
case class EventHandlerRegisteringException(innerException: Throwable)
    extends ScalaBotException("An error has occurred while trying to register an event handler", innerException)
case class ChatException(innerException: Throwable)
    extends ScalaBotException("An error has occurred while accessing the chat", innerException)
