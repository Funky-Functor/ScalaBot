package com.funkyfunctor.scalabot.botsh

import fastparse._
import zio.random.Random
import zio.test.Assertion._
import zio.test.TestAspect.flaky
import zio.test._

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}
import scala.util.Try

object BotShellEvaluatorTest extends DefaultRunnableSpec {
  val variableNameGen: Gen[Random with Sized, String] = for {
    firstCharacter <- Gen.alphaChar
    remainder      <- Gen.alphaNumericString
  } yield "" + firstCharacter + remainder

  override def spec: ZSpec[Environment, Failure] = suite("BotShellEvaluator")(
    parserTests,
    builtInMethodsTests
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
      context: Map[String, Value] = Value.builtInFunctions,
      parser: P[_] => P[Expr] = Parsers.expr(_)
  ): Assert = {
    testExpressionEvaluationWithCondition(input, _ == expected, context, parser)
  }

  def testExpressionEvaluationWithCondition(
      input: String,
      expected: String => Boolean,
      context: Map[String, Value] = Value.builtInFunctions,
      parser: P[_] => P[Expr] = Parsers.expr(_)
  ): Assert = {
    val (assertion01, exprOpt) = testExpressionParsingAndReturnExpression(input, parser)

    exprOpt match {
      case None => assertion01
      case Some(astExpr) =>
        val evaluationResult = Value.evaluate(astExpr, context)

        if (!expected(evaluationResult.toString))
          System.err.println(s"AST for '$input' is '$astExpr' ==> '${evaluationResult.toString}'")

        val assertion02 = assertTrue(expected(evaluationResult.toString))

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
    test01_11,
    test01_12,
    test01_13
  )

  private val builtInMethodsTests = suite("Built-in methods tests")(
    test02_01,
    test02_02,
    test02_03
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

  private lazy val test01_12 = test("Evaluating operator precedence") {
    testExpressionEvaluation("1 + 2 * 3 / 6", "Str(2)")
  }

  private lazy val test01_13 = test("Evaluating conditions") {
    testExpressionEvaluation("if (\"true\") then 1 else 0;", "Str(1)") &&
    testExpressionEvaluation("if (\"false\") then 1 else 0;", "Str(0)") &&
    testExpressionEvaluation("if (\"true\") then 1;", "Str(1)") &&
    testExpressionEvaluation("if (\"false\") then 1;", "Str()") &&
    testExpressionEvaluation("if (true) then 1 else 0;", "Str(1)") &&
    testExpressionEvaluation("if (false) then 1 else 0;", "Str(0)") &&
    testExpressionEvaluation("if (1) then 1 else 0;", "Str(1)") &&
    testExpressionEvaluation("if (0) then 1 else 0;", "Str(0)") &&
    testExpressionEvaluation("if (\"tr\" + \"ue\") then 1 else 0;", "Str(1)") &&
    testExpressionEvaluation("if (\"fal\" + \"se\") then 1 else 0;", "Str(0)") &&
    testExpressionEvaluation("if (true) then 1 + 0 else 0 + 2;", "Str(1)") &&
    testExpressionEvaluation("if (false) then 1 + 0 else 0 + 2;", "Str(2)") &&
    testExpressionEvaluation("if (if (true) then 1;) then 2 else 3;", "Str(2)") &&
    testExpressionEvaluation("if (true) then if (true) then 1; else 0;", "Str(1)") &&
    testExpressionEvaluation("if (true) then if (false) then 0 else 1; else 0;", "Str(1)") &&
    testExpressionEvaluation("if (false) then 0 else if (true) then 1;;", "Str(1)") &&
    testExpressionEvaluation("if (false) then 0 else if (false) then 0 else 1;;", "Str(1)")
  }

  private lazy val test02_01 = test("Simple built-in methods tests") {
    testExpressionEvaluation("defaultDateFormat()", s"Str(${BuiltInFunctions.defaultDateFormat})") &&
    testExpressionEvaluation(
      "defaultDateFormat(1, 2)",
      "Error(Incorrect number of arguments: 2 (expected size: 0)," +
        "Some(java.lang.Exception: Incorrect number of arguments: 2 (expected size: 0)))"
    ) &&
    testExpressionEvaluation("defaultTimezone()", s"Str(${BuiltInFunctions.defaultTimezone})") &&
    testExpressionEvaluation(
      "defaultTimezone(1, 2, 3)",
      "Error(Incorrect number of arguments: 3 (expected size: 0)," +
        "Some(java.lang.Exception: Incorrect number of arguments: 3 (expected size: 0)))"
    )
  }

  private lazy val test02_02 = test("'now(...)' built-in method tests") {
    val testDateFormat = "dd-MM-yyyy HH:mm"
    val testTimezone   = "EST"

    val expectedTime01 = {
      val df = new SimpleDateFormat(testDateFormat)
      df.setTimeZone(TimeZone.getTimeZone(testTimezone))
      df.format(new Date())
    }

    val expectedTime02 = {
      val df = new SimpleDateFormat(testDateFormat)
      df.setTimeZone(TimeZone.getTimeZone(BuiltInFunctions.defaultTimezone))
      df.format(new Date())
    }

    val expectation03 = { str: String =>
      val regex = """Str\(\d{2}\-\d{2}\-\d{4} \d{2}:\d{2}:\d{2} UTC\)"""
      val startWith = s"Str($expectedTime02"
      val startsWithCondition = str.startsWith(startWith)
      val regexCondition = str.matches(regex)

      (startsWithCondition, regexCondition) match {
        case (true, true) =>
        case (true, false) => System.err.println(s"'$str' does not match the regex '$regex'")
        case (false, true) => System.err.println(s"'$str' does not start with '$startWith'")
        case (false, false) => System.err.println(s"'$str' does not match the regex '$regex' and does not start with '$startWith'")
      }

      startsWithCondition && regexCondition
    }

    testExpressionEvaluation(
      "now(1, 2, 3)",
      "Error(Incorrect number of arguments: 3 (expected min size: 0, expected max size: 2)," +
        "Some(java.lang.Exception: Incorrect number of arguments: 3 (expected min size: 0, expected max size: 2)))"
    ) &&
    testExpressionEvaluation(s"now(\"$testDateFormat\", \"$testTimezone\")", s"Str($expectedTime01)") &&
    testExpressionEvaluation(s"now(\"$testDateFormat\")", s"Str($expectedTime02)") &&
    testExpressionEvaluationWithCondition(s"now()", expectation03)
  } @@ flaky // We are testing the date and the returned date could be different from expectations in certain cases (day change or other)

  private lazy val test02_03 = test("'iff(...)' built-in method tests") {
    testExpressionEvaluation("iff (\"true\", 1, 0)", "Str(1)") &&
    testExpressionEvaluation("iff (\"false\", 1, 0)", "Str(0)") &&
    testExpressionEvaluation("iff (\"true\", 1)", "Str(1)") &&
    testExpressionEvaluation("iff (\"false\", 1)", "Str()") &&
    testExpressionEvaluation("iff (true, 1, 0)", "Str(1)") &&
    testExpressionEvaluation("iff (false, 1, 0)", "Str(0)") &&
    testExpressionEvaluation("iff (1, 1, 0)", "Str(1)") &&
    testExpressionEvaluation("iff (0, 1,  0)", "Str(0)") &&
    testExpressionEvaluation("iff (\"tr\" + \"ue\", 1,  0)", "Str(1)") &&
    testExpressionEvaluation("iff (\"fal\" + \"se\", 1,  0)", "Str(0)") &&
    testExpressionEvaluation("iff (true, 1 + 0, 0 + 2)", "Str(1)") &&
    testExpressionEvaluation("iff (false, 1 + 0, 0 + 2)", "Str(2)") &&
    testExpressionEvaluation("iff (iff (true, 1), 2, 3)", "Str(2)") &&
    testExpressionEvaluation("iff (true, iff (true, 1), 0)", "Str(1)") &&
    testExpressionEvaluation("iff (true, iff (false, 0, 1), 0)", "Str(1)") &&
    testExpressionEvaluation("iff (false, 0, iff (true, 1))", "Str(1)") &&
    testExpressionEvaluation("iff (false, 0, iff (false, 0, 1))", "Str(1)")
  }
}
