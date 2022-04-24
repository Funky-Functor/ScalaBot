package com.funkyfunctor.scalabot.commands

import com.funkyfunctor.scalabot.Main.ScalabotEnvironment
import zio.test._

object CommandTest extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] = suite("Command")(
    getCommandStringTests,
    toCommandSeqTests
  )

  private val getCommandStringTests = suite("getCommandString(command: String)")(
    test1_01,
    test1_02,
    test1_03,
    test1_04
  )

  private val toCommandSeqTests = suite("toCommandSeq(command: String)")(
    test2_01,
    test2_02,
    test2_03,
    test2_04
  )

  private lazy val test1_01 = test(""""!hello world" ==> "world"""") {
    val constructor = new CommandConstructor {
      override def commandKey: String = "!hello"

      override def getCommand(commandArguments: String, context: CommandContext): ScalabotEnvironment[Command] = ???
    }

    assertTrue(constructor.getCommandString("!hello world") == "world")
  }

  private lazy val test1_02 = test(""""!hello beautiful world" ==> "beautiful world"""") {
    val constructor = new CommandConstructor {
      override def commandKey: String = "!hello"

      override def getCommand(commandArguments: String, context: CommandContext): ScalabotEnvironment[Command] = ???
    }

    assertTrue(constructor.getCommandString("!hello beautiful world") == "beautiful world")
  }

  private lazy val test1_03 = test(""""I said !hello world" => "world"""") {
    val constructor = new CommandConstructor {
      override def commandKey: String = "!hello"

      override def getCommand(commandArguments: String, context: CommandContext): ScalabotEnvironment[Command] = ???
    }

    assertTrue(constructor.getCommandString("I said !hello world") == "world")
  }

  private lazy val test1_04 = test(""""!ping world" ==> "!ping world"""") {
    val constructor = new CommandConstructor {
      override def commandKey: String = "!hello"

      override def getCommand(commandArguments: String, context: CommandContext): ScalabotEnvironment[Command] = ???
    }

    assertTrue(constructor.getCommandString("!ping world") == "!ping world")
  }

  private lazy val test2_01 = test(""""!hello world" => "!hello world"""") {
    val input = "!hello world"

    val result = Command.toCommandSeq(input).mkString(" ")

    if (result != "!hello world") {
      System.err.println(s"test2_01 - Result for '$input' is '$result'")
    }

    assertTrue(result == "!hello world")
  }

  private lazy val test2_02 = test(""""" => """"") {
    val input = ""

    val result = Command.toCommandSeq(input).mkString(" ")

    if (result != "") {
      System.err.println(s"test2_02 - Result for '$input' is '$result'")
    }

    assertTrue(result == "")
  }

  private lazy val test2_03 = test(""""hello world" => """"") {
    val input = "hello world"

    val result = Command.toCommandSeq(input).mkString(" ")

    if (result != "") {
      System.err.println(s"test2_03 - Result for '$input' is '$result'")
    }

    assertTrue(result == "")
  }

  private lazy val test2_04 = test(""""I said !hello world" => "!hello world"""") {
    val input = "I said !hello world"

    val result = Command.toCommandSeq(input).mkString(" ")

    if (result != "!hello world") {
      System.err.println(s"test2_04 - Result for '$input' is '$result'")
    }

    assertTrue(result == "!hello world")
  }
}
