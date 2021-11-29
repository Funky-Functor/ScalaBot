package com.funkyfunctor.scalabot.botsh

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

  val builtInFunctionsMap: Map[String, Seq[String] => String] = Map(
    BuiltInFunctions.nowName               -> now,
    BuiltInFunctions.defaultDateFormatName -> getDefaultDateFormat,
    BuiltInFunctions.defaultTimezoneName   -> getDefaultTimezone
  )

  def getDefaultDateFormat(args: Seq[String]): String = {
    defaultDateFormat
  }

  def getDefaultTimezone(args: Seq[String]): String = {
    defaultTimezone
  }

  def now(args: Seq[String]): String = {
    val (format, timezone) = args match {
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

    formatter.format(new Date())
  }
}
