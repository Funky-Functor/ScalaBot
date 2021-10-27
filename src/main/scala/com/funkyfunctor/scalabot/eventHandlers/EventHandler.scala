package com.funkyfunctor.scalabot.eventHandlers

import com.funkyfunctor.scalabot.{
  EventHandlerRegisteringException,
  EventHandlerRegistrar,
  JavaEventHandler,
  ScalaBotException
}
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.AbstractChannelEvent
import zio.ZIO
import zio.logging.Logging

import java.util.function.Consumer
import scala.reflect.ClassTag

object EventHandler {
  def registerHandlers(
      twitchClient: TwitchClient,
      eventHandlers: Seq[EventHandler[_]]
  ): ZIO[Logging, ScalaBotException, Unit] = {
    for {
      eventManager <- ZIO { twitchClient.getEventManager }
      _ <- ZIO.foreach_(eventHandlers) { eventHandler =>
        ZIO(EventHandlerRegistrar.registerHandlerToEventManager(eventManager, eventHandler))
      }
    } yield ()
  }.mapError(error => EventHandlerRegisteringException(error))
}

class EventHandler[E <: AbstractChannelEvent](consumerFunction: E => Unit)(implicit tag: ClassTag[E])
    extends JavaEventHandler[E] {
  override def getConsumer: Consumer[E] = (t: E) => consumerFunction(t)

  override def getClazz(): Class[E] = tag.runtimeClass.asInstanceOf[Class[E]]
}
