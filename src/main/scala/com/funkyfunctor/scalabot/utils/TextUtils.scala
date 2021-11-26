package com.funkyfunctor.scalabot.utils

object TextUtils {
  private[utils] val flipMap = Map(
    'a' -> 'ɐ',
    'b' -> 'q',
    'c' -> 'ɔ',
    'd' -> 'p',
    'e' -> 'ǝ',
    'f' -> 'ɟ',
    'g' -> 'ƃ',
    'h' -> 'ɥ',
    'i' -> 'ᴉ',
    'j' -> 'ɾ',
    'k' -> 'ʞ',
    'l' -> 'l',
    'm' -> 'ɯ',
    'n' -> 'u',
    'o' -> 'o',
    'p' -> 'd',
    'q' -> 'b',
    'r' -> 'ɹ',
    's' -> 's',
    't' -> 'ʇ',
    'u' -> 'n',
    'v' -> 'ʌ',
    'w' -> 'ʍ',
    'x' -> 'x',
    'y' -> 'ʎ',
    'z' -> 'z',
    'A' -> '∀',
    'B' -> 'q',
    'C' -> 'Ɔ',
    'D' -> 'p',
    'E' -> 'Ǝ',
    'F' -> 'Ⅎ',
    'G' -> 'פ',
    'H' -> 'H',
    'I' -> 'I',
    'J' -> 'ſ',
    'K' -> 'ʞ',
    'L' -> '˥',
    'M' -> 'W',
    'N' -> 'N',
    'O' -> 'O',
    'P' -> 'Ԁ',
    'Q' -> 'Q',
    'R' -> 'ɹ',
    'S' -> 'S',
    'T' -> '┴',
    'U' -> '∩',
    'V' -> 'Λ',
    'W' -> 'M',
    'X' -> 'X',
    'Y' -> '⅄',
    'Z' -> 'Z',
    '0' -> '0',
    '1' -> '⇂',
    '2' -> 'ᘔ',
    '3' -> 'Ɛ',
    '4' -> '߈',
    '5' -> 'ဌ',
    '6' -> '9',
    '7' -> 'ㄥ',
    '8' -> '8',
    '9' -> '6',
    '_' -> '‾',
    '!' -> '¡',
    '.' -> '˙',
    ',' -> '\''
  )

  def flip(text: String): String = text.map(char => flipMap.getOrElse(char, char))

  def reverse(text: String): String = text.reverse

  def rflip(text: String): String = flip(reverse(text))

  private[utils] val lowerCaseVowels: Set[Char] = Set('a', 'e', 'i', 'o', 'u', 'y')
  private[utils] val upperCaseVowels: Set[Char] = Set('A', 'E', 'I', 'O', 'U', 'Y')
  private[utils] val vowels: Set[Char]          = lowerCaseVowels ++ upperCaseVowels

  private def toPigWord(priorChars: List[Char], remainingChars: List[Char]): String =
    (priorChars, remainingChars) match {
      case (Nil, Nil)                                 => ""
      case (_, Nil)                                   => priorChars.mkString + "ay"
      case (_, head :: tail) if vowels.contains(head) => head.toString + tail.mkString + priorChars.mkString + "ay"
      case (_, head :: tail)                          => toPigWord(priorChars :+ head, tail)
    }

  private def toPigWord(word: String): String =
    word.toList match {
      case Nil                                   => ""
      case head :: tail if head == 'y'           => "yay" + tail.mkString
      case head :: tail if head == 'Y'           => "Yay" + tail.mkString
      case head :: tail if vowels.contains(head) => word + "ay"
      case charList                              => toPigWord(Nil, charList)
    }

  def translate(text: String): String =
    text
      .replaceAll("[^a-zA-Z ]", "")
      .split(" ")
      .map(toPigWord)
      .mkString(" ")
}
