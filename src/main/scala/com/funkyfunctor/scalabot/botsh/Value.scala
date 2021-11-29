package com.funkyfunctor.scalabot.botsh

import scala.util.Try

sealed trait Value

object Value {
  case class Str(s: String)                           extends Value
  case class Error(msg: String, e: Option[Throwable]) extends Value
  // case class Dict(pairs: Map[String, Value]) extends Value
  case class Func(call: Seq[Value] => Value) extends Value

  def evaluate(expr: Expr, scope: Map[String, Value]): Value = expr match {
    case Expr.Str(s) => Value.Str(s)
    // case Expr.Dict(kvs) => Value.Dict(kvs.map { case (k, v) => (k, evaluate(v, scope)) })
    case Expr.BinaryOperator(head, tail) =>
      applyOperator(
        evaluate(head, scope),
        tail.map { case (operatorStr, expr) =>
          operatorStr -> evaluate(expr, scope)
        }
      )
    // case Expr.Sub(head, tail)  => substractSeq(evaluate(head, scope), tail.map(evaluate(_, scope)))
    case Expr.Val(name, assigned, body) =>
      val b             = body.getOrElse(assigned)
      val assignedValue = evaluate(assigned, scope)
      evaluate(b, scope + (name -> assignedValue))
    case Expr.Ident(name) => scope(name)
    case Expr.Call(expr, args) =>
      val Value.Func(call) = evaluate(expr, scope)
      val evaluatedArgs    = args.map(evaluate(_, scope))
      call(evaluatedArgs)
    case Expr.Func(argNames, body) =>
      Value.Func(args => evaluate(body, scope ++ argNames.zip(args)))
  }

  def strToBigDecimal(str: String): Option[BigDecimal] = Try { BigDecimal(str) }.toOption

  def strToBigDecimal(str: String, default: => BigDecimal): BigDecimal = strToBigDecimal(str).getOrElse(default)

  private def applyOperator(headValue: Value, tailValue: Seq[(String, Value)]): Value = {
    val head: String = headValue match {
      case Value.Str(str) => str
      case _              => "0"
    }

    val tail = tailValue.map { case (op, Value.Str(s)) => (op, s) }

    Try {tail.foldLeft(head) { (acc, tuple) =>
      val (operator, e) = tuple
      val e1            = strToBigDecimal(acc)
      val e2            = strToBigDecimal(e)

      operator match {
        case "+" =>
          (e1, e2) match {
            case (Some(n1), Some(n2)) => (n1 + n2).toString()
            case _                    => acc + e
          }
        case "-" =>
          (e1, e2) match {
            case (Some(n1), Some(n2)) => (n1 - n2).toString()
            case (Some(n1), None)     => n1.toString()
            case (None, Some(n2))     => (-n2).toString()
            case (None, None)         => "0"
          }
        case "*" =>
          (e1, e2) match {
            case (Some(n1), Some(n2)) => (n1 * n2).toString()
            case (Some(n1), None)     => List.fill(n1.toInt)(e).mkString
            case (None, Some(n2))     => List.fill(n2.toInt)(acc).mkString
            case (None, None)         => throw new Exception(s"Trying to multiply two strings ('$acc' and '$e')")
          }
        case _ =>
          (e1, e2) match {
            case (Some(n1), Some(n2)) if n2 != 0 => (n1 / n2).toString()
            case (Some(n1), Some(n2)) if n2 == 0 => throw new Exception(s"Trying to divide a $n1 by 0")
            case (Some(n1), None)     => throw new Exception(s"Trying to divide $n1 by a string('$e')'")
            case (None, Some(n2))     => throw new Exception(s"Trying to divide a string('$acc') by $n2")
            case (None, None)         => throw new Exception(s"Trying to divide two strings ('$acc' and '$e')")
          }
      }
    }}.fold(
      exc => Value.Error(exc.getMessage, Some(exc)),
      str => Value.Str(str)
    )
  }

//  private def substractSeq(headValue: Value, tailValue: Seq[Value]): Value.Str = {
//    val head: String = headValue match {
//      case Value.Str(str) => str
//      case _              => "0"
//    }
//
//    val tail = tailValue.map { case Value.Str(s) => s }
//
//    Value.Str(
//      tail
//        .foldLeft(head) { case (e1, e2) =>
//          val firstArg = strToBigDecimal(e1, BigDecimal(0))
//          val secondArg =strToBigDecimal(e2, BigDecimal(0))
//
//          System.err.println(s"Value for `$e1` is '$firstArg' - value for '$e2' is '$secondArg'")
//
//          (firstArg - secondArg).toString()
//        }
//    )
//  }

  private def multiplySeq(seq: Seq[String]): Value = seq match {
    case Nil => Value.Str("0")
    case head :: tail =>
      Value.Str(
        tail
          .foldLeft(head) { case (e1, e2) =>
            (strToBigDecimal(e1, BigDecimal(1)) * strToBigDecimal(e2, BigDecimal(1))).toString()
          }
      )
  }
}
