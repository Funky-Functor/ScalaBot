package com.funkyfunctor.scalabot.botsh

import com.funkyfunctor.scalabot.commands.CommandContext
import fastparse.NoWhitespace._
import fastparse._

case class BotShellInput(command: String, context: CommandContext)
case class BotShellOutput(response: Option[String], context: CommandContext)

object BotShellEvaluator {
  def evaluate(input: BotShellInput): BotShellOutput = ???

  def escapeString(str: String): String = str.replace("\"", "\\\"")


}
