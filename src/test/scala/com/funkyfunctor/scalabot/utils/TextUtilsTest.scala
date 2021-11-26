package com.funkyfunctor.scalabot.utils

import com.funkyfunctor.scalabot.test.TestUtils._
import zio._
import zio.random.Random
import zio.test.Assertion._
import zio.test._

object TextUtilsTest extends DefaultRunnableSpec {
  private val keys = TextUtils.flipMap.keySet

  private val normalCharacters: NonEmptyChunk[Char] =
    NonEmptyChunk.fromIterable(keys.head, keys.tail)

  private val flippedCharacters: NonEmptyChunk[Char] = {
    val values = TextUtils.flipMap.values.toSet
    NonEmptyChunk.fromIterable(values.head, values.tail)
  }

  private val normalCharactersStringGen = Gen.string(genFromIterable(normalCharacters))
  private val nonFlippableCharacterStringGen: Gen[Random with Sized, String] = Gen.string(
    Gen.anyChar.filterNot(keys.contains)
  )

  override def spec: ZSpec[Environment, Failure] = suite("TextUtilsTest")(
    flipTests,
    reverseTests,
    rflipTests,
    translateTests
  )

  private val flipTests = suite("flip(String)")(test01, test02, test03)

  private lazy val test01 = test("should return 'ɥǝllo ʍoɹlp' when we pass 'hello world' as input") {
    val input = "hello world"

    val result = TextUtils.flip(input)

    assertTrue(result == "ɥǝllo ʍoɹlp")
  }

  private lazy val test02 = testM(
    "for a given string made of alphanumeric characters, should return a string of 'flipped' characters"
  ) {
    check(normalCharactersStringGen) { input =>
      val result = TextUtils.flip(input)

      assert(result)(hasSizeString(equalTo(input.length))) &&
      assert(result.toIndexedSeq)(forall(isOneOf(flippedCharacters)))
    }
  }

  private lazy val test03 = testM("should leave non flippable strings as is") {
    check(nonFlippableCharacterStringGen) { input =>
      val result = TextUtils.flip(input)

      assertTrue(result == input)
    }
  }

  private val reverseTests = suite("reverse(String)")(test04, test05, test06)

  private lazy val test04 = test("should return 'tset a si sihT' when we pass it 'This is a test'") {
    val input = "This is a test"
    val result = TextUtils.reverse(input)

    assertTrue(result == "tset a si sihT")
  }

  private lazy val test05 = testM("should return a String with the same number of chars") {
    check(Gen.anyString) { input =>
      val result = TextUtils.reverse(input)

      assertTrue(result.length == input.length)
    }
  }

  private lazy val test06 = testM("should return with the same chars as the input but not necessarily in the same order") {
    check(Gen.anyString) { input =>
      val result = TextUtils.reverse(input)

      assert(result.toIndexedSeq)(forall(isOneOf(input.toIndexedSeq)))
    }
  }

  private val rflipTests = suite("rflip(String)")(test07, test08, test09, test10)

  private lazy val test07 = test("should return 'ɥǝllo ʍoɹlp' when we pass 'hello world' as input") {
    val input = "hello world"

    val result = TextUtils.rflip(input)

    assertTrue(result == "plɹoʍ ollǝɥ")
  }

  private lazy val test08 = testM(
    "for a given string made of alphanumeric characters, should return a string of 'flipped' characters"
  ) {
    check(normalCharactersStringGen) { input =>
      val result = TextUtils.rflip(input)

      assert(result)(hasSizeString(equalTo(input.length))) &&
        assert(result.toIndexedSeq)(forall(isOneOf(flippedCharacters)))
    }
  }

  private lazy val test09 = testM("should return a String with the same number of chars") {
    check(Gen.anyString) { input =>
      val result = TextUtils.rflip(input)

      assertTrue(result.length == input.length)
    }
  }

  private lazy val test10 = testM("should return with the same chars as the input but not necessarily in the " +
    "same order if the input is made of non flippable strings") {
    check(nonFlippableCharacterStringGen) { input =>
      val result = TextUtils.rflip(input)

      assert(result.toIndexedSeq)(forall(isOneOf(input.toIndexedSeq)))
    }
  }

  private val translateTests = suite("translate(String)")(test11)

  private lazy val test11 = test("should return 'isthay is aay esttay' when we pass it 'This is a test'") {
    val input = "This is a test"
    val result = TextUtils.translate(input)

    assertTrue(result == "isThay isay aay esttay")
  }
}
