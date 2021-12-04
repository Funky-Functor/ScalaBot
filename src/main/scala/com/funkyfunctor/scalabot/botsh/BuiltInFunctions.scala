package com.funkyfunctor.scalabot.botsh

import com.funkyfunctor.scalabot.botsh.Value.function

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}
import scala.util.Try

object BuiltInFunctions {
  // TODO Load it from config
  val defaultDateFormat = "dd-MM-yyyy HH:mm:SS z"
  val defaultTimezone   = "UTC"

  val nowName               = "now"
  val defaultDateFormatName = "defaultDateFormat"
  val defaultTimezoneName   = "defaultTimezone"
}

case class BuiltInFunctions private (
    defaultDateFormat: String = BuiltInFunctions.defaultDateFormat,
    defaultTimezone: String = BuiltInFunctions.defaultTimezone
) {

//  val builtInFunctionsMap: Map[String, function] = Map(
//    BuiltInFunctions.nowName               -> now(_),
//    BuiltInFunctions.defaultDateFormatName -> getDefaultDateFormat(_),
//    BuiltInFunctions.defaultTimezoneName   -> getDefaultTimezone(_)
//  ).view.mapValues(Value.toFunc).toMap

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
