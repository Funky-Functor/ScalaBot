package com.funkyfunctor.scalabot.botsh

import com.funkyfunctor.scalabot.botsh.Value.isStringTrue

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}
import scala.util.Try

object BuiltInFunctions {
  // TODO Load it from config
  val defaultDateFormat = "dd-MM-yyyy HH:mm:ss z"
  val defaultTimezone   = "UTC"

  val nowName               = "now"
  val defaultDateFormatName = "defaultDateFormat"
  val defaultTimezoneName   = "defaultTimezone"

  val ifFunctionName = "iff"
  val helloWorldName = "helloWorld"
}

case class BuiltInFunctions(
    defaultDateFormat: String = BuiltInFunctions.defaultDateFormat,
    defaultTimezone: String = BuiltInFunctions.defaultTimezone
) {

  val builtInFunctionsMap: Map[String, Seq[String] => Try[String]] = Map(
    BuiltInFunctions.nowName               -> now,
    BuiltInFunctions.defaultDateFormatName -> getDefaultDateFormat,
    BuiltInFunctions.defaultTimezoneName   -> getDefaultTimezone,
    BuiltInFunctions.ifFunctionName        -> ifFunction
  )

  def checkArgumentNumber(args: Seq[String], size: Int = 0)(f: Seq[String] => Try[String]): Try[String] = {
    checkArgumentNumber(args, size, size)(f)
  }

  def checkArgumentNumber(args: Seq[String], min: Int, max: Int)(f: Seq[String] => Try[String]): Try[String] = {
    val l = args.length
    if (l >= min && l <= max)
      f(args)
    else
      Try {
        val expString =
          if (min != max)
            s"(expected min size: $min, expected max size: $max)"
          else s"(expected size: $min)"
        throw new Exception(s"Incorrect number of arguments: $l $expString")
      }
  }

  def ifFunction(args: Seq[String]): Try[String] = {
    checkArgumentNumber(args, 2, 3) { seq =>
      val (condition, ifTrue, ifFalse) = seq match {
        case cond :: ifTrue :: Nil          => (cond, ifTrue, "")
        case cond :: ifTrue :: ifFalse :: _ => (cond, ifTrue, ifFalse)
        case _                              => throw new Exception(s"Incorrect number of arguments: ${seq.length}")
      }

      if (isStringTrue(condition))
        Try(ifTrue)
      else Try(ifFalse)
    }
  }

  def getDefaultDateFormat(args: Seq[String]): Try[String] = {
    checkArgumentNumber(args)(_ => Try(defaultDateFormat))
  }

  def getDefaultTimezone(args: Seq[String]): Try[String] = {
    checkArgumentNumber(args)(_ => Try(defaultTimezone))
  }

  def now(args: Seq[String]): Try[String] = checkArgumentNumber(args, 0, 2) { arguments =>
    val (format, timezone) = arguments match {
      case Nil                 => (defaultDateFormat, defaultTimezone)
      case format :: Nil       => (format, defaultTimezone)
      case format :: zone :: _ => (format, zone)
    }

    val formatter = Try {
      val df = new SimpleDateFormat(format)
      df.setTimeZone(TimeZone.getTimeZone(timezone))
      df
    }.getOrElse {
      val df = new SimpleDateFormat(defaultDateFormat)
      df.setTimeZone(TimeZone.getTimeZone(defaultTimezone))
      df
    }

    Try(formatter.format(new Date()))
  }
}
