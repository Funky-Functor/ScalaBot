package com.funkyfunctor.scalabot.eventHandlers

import com.funkyfunctor.scalabot.CustomLayer.{DefaultCustomLayer, ScalaBotSpecificContext}
import com.funkyfunctor.scalabot.Main.ScalaBotContext
import com.funkyfunctor.scalabot.twitch.{EventHandlerRegistrar, JavaEventHandler}
import com.funkyfunctor.scalabot.{EventHandlerRegisteringException, ScalaBotException}
import com.github.philippheuer.events4j.core.EventManager
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.AbstractChannelEvent
import zio._
import zio.console.putStrLn
import zio.logging.{Logging, log}

import java.util.function.Consumer
import scala.reflect.ClassTag

object EventHandler {
  def registerHandlers(
      twitchClient: TwitchClient,
      eventHandlers: Seq[ZioEventHandler[_]]
  ): ZIO[Logging, ScalaBotException, Unit] = {
    for {
      eventManager <- ZIO {
        twitchClient.getEventManager
      }
      _ <- ZIO.foreach_(eventHandlers) {
        registerHandler(eventManager, _)
      }
    } yield ()
  }.mapError(error => EventHandlerRegisteringException(error))

  private def registerHandler[E](
      eventManager: EventManager,
      handler: ZioEventHandler[_]
  ): ZIO[Logging, Throwable, Unit] =
    log.debug(s"Registering a handler for `${handler.associatedClass}`") *>
      ZIO(EventHandlerRegistrar.registerEventHandler(eventManager, handler))
}

class EventHandler[E <: AbstractChannelEvent](consumerFunction: E => Unit)(implicit tag: ClassTag[E])
    extends JavaEventHandler[E] {
  def associatedClass: Class[E] = tag.runtimeClass.asInstanceOf[Class[E]]

  def consumer: Consumer[E] = (t: E) => consumerFunction(t)

  def t(f: E => ZIO[Any, Throwable, Unit]): Consumer[E] = { (t: E) =>
    Runtime.default.unsafeRun(f(t))
  }
}

class ZioEventHandler[E <: AbstractChannelEvent](
    consumerFunction: E => URIO[ScalaBotContext, Unit],
    scalaBotContext: ZLayer[zio.ZEnv, ScalaBotException, ScalaBotSpecificContext] = DefaultCustomLayer.fullCustomLayer,
    runtime: Runtime[ZEnv] = Runtime.default
)(implicit tag: ClassTag[E])
    extends JavaEventHandler[E] {
  def associatedClass: Class[E] = tag.runtimeClass.asInstanceOf[Class[E]]

  def consumer: Consumer[E] = { (t: E) =>
    val result: ZIO[ZEnv, Nothing, Unit] = consumerFunction(t)
      .provideCustomLayer(scalaBotContext)
      .fold(
        exc => putStrLn(s"An error has occurred while providing the custom layer - '${exc.getMessage}' - $exc"),
        identity
      )
    runtime.unsafeRun(result)
  }
}
