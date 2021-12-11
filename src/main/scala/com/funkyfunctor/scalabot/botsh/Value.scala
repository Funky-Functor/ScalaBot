package com.funkyfunctor.scalabot.botsh

import scala.util.{Failure, Success, Try}

sealed trait Value

object Value {
  type function = Seq[Value] => Value

  case class Str(s: String)                           extends Value
  case class Error(msg: String, e: Option[Throwable]) extends Value
  // case class Dict(pairs: Map[String, Value]) extends Value
  case class Func(call: function) extends Value

  def toFunc(f: Seq[String] => Try[String]): function = {
    val toStrings: Seq[Value] => Seq[String] = s => s.map { case Str(s) => s }

    val handleResult: Try[String] => Value = {
      case Success(str) => Str(str)
      case Failure(e)   => Error(e.getMessage, Some(e))
    }

    s => handleResult(f(toStrings(s)))
  }

  lazy val builtInFunctions: Map[String, Value] = new BuiltInFunctions().builtInFunctionsMap.map { case (key, value) =>
    val function = toFunc(value)
    key -> Func(function)
  }

  def evaluate(expr: Expr, scope: Map[String, Value] = builtInFunctions): Value = expr match {
    case Expr.Str(s) => Value.Str(s)
    // case Expr.Dict(kvs) => Value.Dict(kvs.map { case (k, v) => (k, evaluate(v, scope)) })
    case Expr.AdditionSubtraction(head, tail) =>
      applyAdditionSubstraction(
        evaluate(head, scope),
        tail.map { case (operatorStr, expr) =>
          operatorStr -> evaluate(expr, scope)
        }
      )
    case Expr.MultiplicationDivision(head, tail) =>
      applyMultiplicationDivision(
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
    case Expr.Condition(condExpr, ifTrue, ifFalse) =>
      evaluate(condExpr, scope) match {
        case Str(condStr) =>
          if (isStringTrue(condStr)) {
            evaluate(ifTrue, scope)
          } else {
            ifFalse.map(evaluate(_, scope)).getOrElse(Str(""))
          }
        case otherUnexpectedValue => otherUnexpectedValue
      }
  }

  def isStringTrue(condStr: String): Boolean =
    condStr.equalsIgnoreCase("true") || strToBigDecimal(condStr).exists(_ != 0)

  def strToBigDecimal(str: String): Option[BigDecimal] = Try { BigDecimal(str) }.toOption

  def strToBigDecimal(str: String, default: => BigDecimal): BigDecimal = strToBigDecimal(str).getOrElse(default)

  private def applyMultiplicationDivision(headValue: Value, tailValue: Seq[(String, Value)]): Value = {
    val head: String = headValue match {
      case Value.Str(str) => str
      case _              => "0"
    }

    val tail = tailValue.map { case (op, Value.Str(s)) => (op, s) }

    Try {
      tail.foldLeft(head) { (acc, tuple) =>
        val (operator, e) = tuple
        val e1            = strToBigDecimal(acc)
        val e2            = strToBigDecimal(e)

        operator match {
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
              case (Some(n1), Some(n2)) if n2 == 0 => throw new Exception(s"Trying to divide $n1 by 0")
              case (Some(n1), None)                => throw new Exception(s"Trying to divide $n1 by a string('$e')'")
              case (None, Some(n2))                => throw new Exception(s"Trying to divide a string('$acc') by $n2")
              case (None, None) => throw new Exception(s"Trying to divide two strings ('$acc' and '$e')")
            }
        }
      }
    }.fold(
      exc => Value.Error(exc.getMessage, Some(exc)),
      str => Value.Str(str)
    )
  }

  private def applyAdditionSubstraction(headValue: Value, tailValue: Seq[(String, Value)]): Value = {
    val head: String = headValue match {
      case Value.Str(str) => str
      case _              => "0"
    }

    val tail = tailValue.map { case (op, Value.Str(s)) => (op, s) }

    val str = tail.foldLeft(head) { (acc, tuple) =>
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
      }
    }

    Value.Str(str)
  }
}
