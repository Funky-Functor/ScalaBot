package com.funkyfunctor.scalabot.botsh

import fastparse._
import zio.random.Random
import zio.test.Assertion._
import zio.test._

import scala.util.Try

object BotShellEvaluatorTest extends DefaultRunnableSpec {
  val variableNameGen: Gen[Random with Sized, String] = for {
    firstCharacter <- Gen.alphaChar
    remainder      <- Gen.alphaNumericString
  } yield firstCharacter + remainder

  override def spec: ZSpec[Environment, Failure] = suite("BotShellEvaluator")(
    parserTests
  )

  def testUnparsableExpression(input: String, parser: P[_] => P[Expr] = Parsers.expr(_)): Assert = {
    val expr = fastparse.parse(input, parser)

    if (expr.isSuccess)
      System.err.println(s"Input string is '$input' ==> $expr")

    assertTrue(!expr.isSuccess)
  }

  def testExpressionParsing(input: String, parser: P[_] => P[Expr] = Parsers.expr(_)): TestResult = {
    val expr = fastparse.parse(input, parser)

    assert(expr)(isSubtype[Parsed.Success[Expr]](anything))
  }

  def testExpressionParsingAndReturnExpression(
      input: String,
      parser: P[_] => P[Expr] = Parsers.expr(_)
  ): (Assert, Option[Expr]) = {
    val expr = fastparse.parse(input, parser)

    if (!expr.isSuccess)
      System.err.println(s"Input string is '$input' ==> $expr")

    val assertion = assertTrue(expr.isSuccess)
    assertion -> Try(expr.get.value).toOption
  }

  def testExpressionEvaluation(
      input: String,
      expected: String,
      context: Map[String, Value] = Map(),
      parser: P[_] => P[Expr] = Parsers.expr(_)
  ): Assert = {
    val (assertion01, exprOpt) = testExpressionParsingAndReturnExpression(input, parser)

    exprOpt match {
      case None => assertion01
      case Some(astExpr) =>
        val evaluationResult = Value.evaluate(astExpr, context)

        if (evaluationResult.toString != expected)
          System.err.println(s"AST for '$input' is '$astExpr'")

        val assertion02 = assertTrue(evaluationResult.toString == expected)

        assertion01 && assertion02
    }
  }

  private val parserTests = suite("Parser tests")(
    test01_01,
    test01_02,
    test01_03,
    test01_04,
    test01_05,
    test01_06,
    test01_07,
    test01_08,
    test01_09,
    test01_10,
    test01_11
  )

  private lazy val test01_01 = test("Parsing text") {
    testExpressionEvaluation("\"Hello\"", "Str(Hello)") &&
    testExpressionEvaluation("123", "Str(123)") &&
    testExpressionEvaluation("1.23", "Str(1.23)") &&
    testExpressionEvaluation(".23", "Str(.23)") &&
    testExpressionEvaluation("1.", "Str(1.)") &&
    testExpressionEvaluation("+123", "Str(123)") &&
    testExpressionEvaluation("-123", "Str(-123)") &&
    testExpressionEvaluation("+1.23", "Str(1.23)") &&
    testExpressionEvaluation("-1.23", "Str(-1.23)") &&
    testUnparsableExpression("\"Hello") // Missing ending double-quotes
  }

  private lazy val test01_02 = test("Parsing variables assignations") {
    testExpressionEvaluation("val test = 123; test", "Str(123)") &&
    testExpressionEvaluation("val _test = 123; _test", "Str(123)") &&
    testExpressionEvaluation("val test = 123;", "Str(123)") && // Empty body, we use the latest assignment value
    testUnparsableExpression("val 1test = 123; 1test") &&      // Bad variable name
    testUnparsableExpression("val test 123; test") &&          // Missing '='
    testUnparsableExpression("val test 123; test") &&          // Missing '='
    testUnparsableExpression("val test = ; test")              // Missing value
  }

  private lazy val test01_03 = test("Parsing function definitions") {
    testExpressionEvaluation("val f = function (t) t; f(123)", "Str(123)") &&
    testExpressionEvaluation("val f = function () 123; f()", "Str(123)") &&
    testUnparsableExpression("val f = function (t t; f(123)") && // Forgetting closing parenthesis
    testUnparsableExpression("val f = function (t); f(123)")     // Forgetting method body
  }

  private lazy val test01_04 = test("Evaluating additions - simple") {
    testExpressionEvaluation("1 + 1", "Str(2)") &&
    testUnparsableExpression("+ 1") &&
    testUnparsableExpression("+ -1") &&
    testExpressionEvaluation("-1 + 1", "Str(0)") &&
    testExpressionEvaluation("1 + -1", "Str(0)") &&
    testExpressionEvaluation("\"Hello\" + 1", "Str(Hello1)") &&
    testExpressionEvaluation("1 + \"Hello\"", "Str(1Hello)") &&
    testExpressionEvaluation("\"Hello\" + \" World\"", "Str(Hello World)")
  }

  private lazy val test01_05 = test("Evaluating additions - advanced") {
    testExpressionEvaluation("val t = 1; val u = 1; t + u", "Str(2)") &&
    testExpressionEvaluation("val f1 = function () 1; val f2 = f1(); f1() + f2", "Str(2)") &&
    testExpressionEvaluation("val f1 = function () 1; val f2 = function (a) a; f1() + f2(1)", "Str(2)")
  }

  private lazy val test01_06 = test("Evaluating substractions - simple") {
    testExpressionEvaluation("2 - 1", "Str(1)") &&
    testUnparsableExpression("- 1") &&
    testUnparsableExpression("- -1") &&
    testExpressionEvaluation("-1 - 1", "Str(-2)") &&
    testExpressionEvaluation("1 - -1", "Str(2)") &&
    testExpressionEvaluation("\"Hello\" - 1", "Str(-1)") &&
    testExpressionEvaluation("1 - \"Hello\"", "Str(1)") &&
    testExpressionEvaluation("\"Hello\" - \" World\"", "Str(0)")
  }

  private lazy val test01_07 = test("Evaluating substractions - advanced") {
    testExpressionEvaluation("val t = 1; val u = 1; t - u", "Str(0)") &&
    testExpressionEvaluation("val f1 = function () 1; val f2 = f1(); f1() - f2", "Str(0)") &&
    testExpressionEvaluation("val f1 = function () 1; val f2 = function (a) a; f1() - f2(1)", "Str(0)")
  }

  private lazy val test01_08 = test("Evaluating multiplications - simple") {
    testExpressionEvaluation("2 * 1", "Str(2)") &&
    testUnparsableExpression("* 1") &&
    testUnparsableExpression("* -1") &&
    testExpressionEvaluation("-1 * 1", "Str(-1)") &&
    testExpressionEvaluation("1 * -1", "Str(-1)") &&
    testExpressionEvaluation("\"Hello\" * 2", "Str(HelloHello)") &&
    testExpressionEvaluation("2 * \"Hello\"", "Str(HelloHello)") &&
    testExpressionEvaluation(
      "\"Hello\" * \" World\"",
      "Error(Trying to multiply two strings ('Hello' and ' World'),Some(java.lang.Exception: Trying to multiply two strings ('Hello' and ' World')))"
    )
  }

  private lazy val test01_09 = test("Evaluating multiplication - advanced") {
    testExpressionEvaluation("val t = 1; val u = 1; t * u", "Str(1)") &&
    testExpressionEvaluation("val f1 = function () 1; val f2 = f1(); f1() * f2", "Str(1)") &&
    testExpressionEvaluation("val f1 = function () 1; val f2 = function (a) a; f1() * f2(1)", "Str(1)")
  }

  private lazy val test01_10 = test("Evaluating divisions - simple") {
    testExpressionEvaluation("2 / 1", "Str(2)") &&
    testUnparsableExpression("* 1") &&
    testUnparsableExpression("* -1") &&
    testExpressionEvaluation("-1 / 1", "Str(-1)") &&
    testExpressionEvaluation("1 / -1", "Str(-1)") &&
    testExpressionEvaluation(
      "\"Hello\" / 2",
      "Error(Trying to divide a string('Hello') by 2,Some(java.lang.Exception: Trying to divide a string('Hello') by 2))"
    ) &&
    testExpressionEvaluation(
      "2 / \"Hello\"",
      "Error(Trying to divide 2 by a string('Hello')',Some(java.lang.Exception: Trying to divide 2 by a string('Hello')'))"
    ) &&
    testExpressionEvaluation(
      "\"Hello\" / \" World\"",
      "Error(Trying to divide two strings ('Hello' and ' World'),Some(java.lang.Exception: Trying to divide two strings ('Hello' and ' World')))"
    )
  }

  private lazy val test01_11 = test("Evaluating divisions - advanced") {
    testExpressionEvaluation("val t = 1; val u = 1; t / u", "Str(1)") &&
    testExpressionEvaluation("val f1 = function () 1; val f2 = f1(); f1() / f2", "Str(1)") &&
    testExpressionEvaluation("val f1 = function () 1; val f2 = function (a) a; f1() / f2(1)", "Str(1)")
  }
}
