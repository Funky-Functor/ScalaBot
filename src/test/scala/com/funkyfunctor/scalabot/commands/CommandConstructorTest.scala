package com.funkyfunctor.scalabot.commands

import com.funkyfunctor.scalabot.Main.ScalabotEnvironment
import zio.test._

object CommandConstructorTest extends DefaultRunnableSpec{
  override def spec: ZSpec[Environment, Failure] = suite("CommandConstructor")(
    canAccessTests
  )

  private val canAccessTests = suite("getCommandString(command: String)")(
    test1_01,
    test1_02,
    test1_03,
    test1_04
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
}
