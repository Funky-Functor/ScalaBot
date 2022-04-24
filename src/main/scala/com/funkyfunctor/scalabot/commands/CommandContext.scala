package com.funkyfunctor.scalabot.commands

import com.funkyfunctor.scalabot.Main.ScalabotEnvironment
import com.funkyfunctor.scalabot.MessageReceivedException
import zio.ZIO
import zio.logging.log

object CommandContext {
  val CHAT_CLIENT_KEY = "TwitchChat"
  val CHANNEL_KEY     = "channel"
}

case class CommandContext(
    envVariables: Map[String, Any] = Map.empty,
    userVariables: Map[String, Any] = Map.empty
) {
  lazy val contextMap: Map[String, Any] = envVariables ++ userVariables

  def getElement[E](key: String): ScalabotEnvironment[E] = {
    for {
      elementOpt <- ZIO(contextMap.get(key))
      elementObj <- ZIO.fromOption(elementOpt).flatMapError { _ =>
        log
          .info(s"No element associated to '$key' found in the context - '$contextMap'")
          .as(new Exception("No chat client found in the context"))
      }
      element <- ZIO(elementObj.asInstanceOf[E]).mapError(exc =>
        new Exception(s"Got a unexpected object type - '${elementObj.getClass}'")
      )
    } yield element
  }.mapError(MessageReceivedException)
}
