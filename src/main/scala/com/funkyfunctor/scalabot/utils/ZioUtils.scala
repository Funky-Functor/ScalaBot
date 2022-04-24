package com.funkyfunctor.scalabot.utils

import com.funkyfunctor.scalabot.Main.ScalaBotContext
import zio._
import zio.logging.log

object ZioUtils {
  def processZio[A](
      zio: ZIO[ScalaBotContext, Throwable, A],
      errorMessage: => String
  ): ZIO[ScalaBotContext, Nothing, Unit] = {
    zio.foldM(
      exc => log.error(errorMessage, Cause.die(exc)),
      _ => ZIO.unit
    )
  }
}
