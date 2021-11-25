package com.funkyfunctor.scalabot.eventHandlers

import com.funkyfunctor.scalabot.twitch.{EventHandlerRegistrar, JavaEventHandler}
import com.funkyfunctor.scalabot.{EventHandlerRegisteringException, ScalaBotException}
import com.github.philippheuer.events4j.core.EventManager
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.AbstractChannelEvent
import zio.ZIO
import zio.logging.{Logging, log}

import java.util.function.Consumer
import scala.reflect.ClassTag

object EventHandler:
  def registerHandlers(
      twitchClient: TwitchClient,
      eventHandlers: Seq[EventHandler[_]]
  ): ZIO[Logging, ScalaBotException, Unit] = {
    for
      eventManager <- ZIO { twitchClient.getEventManager }
      _            <- ZIO.foreach_(eventHandlers) { registerHandler(eventManager, _) }
    yield ()
  }.mapError(error => EventHandlerRegisteringException(error))

  private def registerHandler[E](
      eventManager: EventManager,
      handler: EventHandler[_]
  ): ZIO[Logging, Throwable, Unit] =
    log.debug(s"Registering a handler for `${handler.associatedClass}`") *>
      ZIO(EventHandlerRegistrar.registerEventHandler(eventManager, handler))
end EventHandler //End of the companion object

class EventHandler[E <: AbstractChannelEvent](consumerFunction: E => Unit)(implicit tag: ClassTag[E])
    extends JavaEventHandler[E]:
  def associatedClass: Class[E] = tag.runtimeClass.asInstanceOf[Class[E]]

  def consumer: Consumer[E] = (t: E) => consumerFunction(t)
end EventHandler //End of the class
