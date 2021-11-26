package com.funkyfunctor.scalabot.test

import zio._
import zio.random.Random
import zio.test._

object TestUtils {
  def genFromIterable[A](chunk: NonEmptyChunk[A]): Gen[Random, A] = Gen.fromRandom { rnd =>
    rnd
      .nextIntBounded(chunk.size)
      .map(chunk.apply)
  }
}
