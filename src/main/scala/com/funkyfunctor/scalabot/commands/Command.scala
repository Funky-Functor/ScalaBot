package com.funkyfunctor.scalabot.commands

import com.funkyfunctor.scalabot.Main.ScalabotEnvironment
import com.funkyfunctor.scalabot.MessageReceivedException
import zio.ZIO
import zio.logging.log

import scala.annotation.tailrec

object Command {
  val COMMAND_MARKER = "!"

  private val commandsMap: Map[String, CommandConstructor] = Map(
    PingConstructor.getTuple,
    //EvalConstructor.getTuple
  )

  object DoNothingCommand extends Command {
    override def run(): ScalabotEnvironment[Unit] = ZIO.unit
  }

  def toCommandSeq(commandString: String): Seq[String] = {
    val tokens = commandString.split("""\s""").toSeq

    toCommandSeq(tokens)
  }

  @tailrec
  private def toCommandSeq(tokens: Seq[String]): Seq[String] = {
    if (tokens.isEmpty)
      Nil
    else {
      if (tokens.head.startsWith(COMMAND_MARKER))
        tokens
      else toCommandSeq(tokens.tail)
    }
  }

  def toCommand(
      commandString: String,
      context: Map[String, Object]
  ): ScalabotEnvironment[Command] = {
    val commandTokens = toCommandSeq(commandString)

    if (commandTokens.isEmpty)
      ZIO.succeed(DoNothingCommand)
    else {
      for {
        // "!ff_ping     test" => Seq("!ff_ping", "test")
        splitString    <- ZIO(commandTokens)
        constructorOpt <- ZIO(commandsMap.get(splitString.head))
        constructor <- ZIO
          .fromOption(constructorOpt)
          .orElseFail(new Exception(s"Command constructor not found for '${splitString.head}'"))
        command <- constructor
          .getCommand(commandString, CommandContext(envVariables = context))
          .flatMapError { _ =>
            val msg = s"Impossible to create a command for '$commandString'"
            log.error(msg) *>
              log
                .info(s"Error details - command: '$commandString' - splitString: '$splitString'")
                .as(new Exception(msg))
          }
        _ <- log.debug(s"Retrieved command '$command'")
      } yield command
    }.flatMapError { exc =>
      log.debug("Transforming exception to a MessageReceivedException").as(MessageReceivedException(exc))
    }
  }
}

trait CommandConstructor { self =>
  def commandKey: String
  def getCommand(command: String, context: CommandContext): ScalabotEnvironment[Command]

  protected def commandKeyWithMarker: String = Command.COMMAND_MARKER + commandKey.trim

  def getCommandString(command: String): String = {
    if (!command.contains(commandKeyWithMarker)) {
      command
    } else {
      if (command.startsWith(commandKeyWithMarker))
        command.substring(commandKeyWithMarker.length).trim
      else
        getCommandString(command.substring(1))
    }
  }

  def getTuple: (String, CommandConstructor) = commandKeyWithMarker -> self
}

trait Command {
  def run(): ScalabotEnvironment[Unit]
}
