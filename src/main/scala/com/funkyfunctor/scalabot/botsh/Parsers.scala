package com.funkyfunctor.scalabot.botsh

import fastparse.MultiLineWhitespace._
import fastparse.{CharsWhileIn, _}

/** Object containing all the parsers used to parse a text Taken from
  * https://www.lihaoyi.com/post/BuildyourownProgrammingLanguagewithScala.html
  */
object Parsers {
  private[botsh] def str0[_: P]    = P("\"" ~~/ CharsWhile(_ != '"', 0).! ~~ "\"").map { Expr.toStr }
  private[botsh] def ident0[_: P]  = P(CharIn("a-zA-Z_") ~~ CharsWhileIn("a-zA-Z0-9_", 0)).!
  private[botsh] def str[_: P]     = P(number | str0)
  private[botsh] def ident[_: P]   = P(ident0).!.map(Expr.Ident)
  private[botsh] def integer[_: P] = CharIn("+\\-").? ~~ CharsWhileIn("0-9")
  private[botsh] def decimal[_: P] = CharIn("+\\-").? ~~ CharsWhileIn("0-9", 0) ~ "." ~ CharsWhileIn("0-9", 0)
//  private[botsh] def dict[_: P]     = P("{" ~/ (str0 ~ ":" ~/ expr).rep(0, ",") ~ "}").map(kvs => Expr.Dict(kvs.toMap))
  private[botsh] def binaryOperator[_: P]     = P(CharIn("+\\-*/").! ~ prefixExpr)
  private[botsh] def call[_: P]     = P("(" ~/ expr.rep(0, ",") ~ ")")
  private[botsh] def number[_: P]   = P(decimal | integer).!.map { str => Expr.toStr(str.trim) }
  private[botsh] def local[_: P]    = P("val" ~/ ident0 ~ "=" ~ expr ~ ";" ~ expr.?).map(Expr.Val.tupled)
  private[botsh] def func[_: P]     = P("function" ~/ "(" ~ ident0.rep(0, ",") ~ ")" ~ expr).map(Expr.Func.tupled)
  private[botsh] def callExpr[_: P] = P(str | local | func | ident)
  private[botsh] def prefixExpr[_: P]: P[Expr] = P(callExpr ~ call.rep).map { case (e, items) =>
    items.foldLeft(e)((f, args) => Expr.Call(f, args))
  }
  private[botsh] def plusExpr[_: P]: P[Expr] = P(prefixExpr ~ binaryOperator.rep).map {
    case (e, Nil)   => e
    case (e, items) => Expr.BinaryOperator(e, items)
  }
  def expr[_: P]: P[Expr] = P(plusExpr)
}
