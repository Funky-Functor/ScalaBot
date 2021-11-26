package com.funkyfunctor.scalabot.commands

object Command {
  private val commandsMap: Map[String, CommandConstructor] = Map(
    PingConstructor.getTuple
  )

  def toCommand(
      commandString: String,
      context: Map[String, Object]
  ): Option[Command] =
    if (!commandString.startsWith("!"))
      None
    else {
      // "!ff_ping     test" => Seq("!ff_ping", "test")
      val splitString: Seq[String] = commandString.split("""\s""").toIndexedSeq

      for {
        constructor <- commandsMap.get(splitString.head)
        command     <- constructor.getCommand(splitString.tail, context)
      } yield command
    }
}

trait CommandConstructor { self =>
  def commandKey: String
  def getCommand(commandArguments: Seq[String], context: Map[String, Object]): Option[Command]

  def getTuple: (String, CommandConstructor) = commandKey -> self
}

trait Command {
  def run(): Unit
}
