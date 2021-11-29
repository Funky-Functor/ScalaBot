package com.funkyfunctor.scalabot.botsh

sealed trait Expr

object Expr {
  case class Str(s: String) extends Expr
  case class Number(nbStr: String) extends Expr {
    lazy val numericValue: BigDecimal = BigDecimal(nbStr)
  }
  case class Ident(name: String) extends Expr
  case class BinaryOperator(head: Expr, tail: Seq[(String, Expr)]) extends Expr
  case class Dict(pairs: Map[String, Expr]) extends Expr
  case class Val(name: String, assigned: Expr, body: Option[Expr]) extends Expr
  case class Func(argNames: Seq[String], body: Expr) extends Expr
  case class Call(expr: Expr, args: Seq[Expr]) extends Expr

  def toStr(str: String): Str = {
    val trimmed = str.trim

    val cleanStr =
        if (str.startsWith("+"))
          str.substring(1)
        else str

      Expr.Str(cleanStr)
  }
}