package parser

import blueColor
import defaultColor
import violetColor
import whiteColor
import yellowColor

/**
 * Представляет произведения в контекстно-свободной грамматике.
 * В грамматиках этого типа левая сторона производств содержит только один нетерминальный символ.
 *
 * @param ruleNumber номер правила, как в описании грамматики
 * @param leftSide нетерминальный символ в левой части правила
 * @param rightSide терминалы и нетерминалы в правой части
 */
class Rule(private val ruleNumber: Int, val leftSide: NonTerminal, val rightSide: Array<Symbol>) {

    override fun toString() : String {
        var str = " $defaultColor $ruleNumber: $violetColor ${leftSide.name} $defaultColor -> "
        for (s in rightSide)
            str += if(s is Terminal) {
                when {
                    s.name == "EPSILON" -> "$yellowColor EPSILON "
                    s.name == "id" -> "$blueColor id "
                    s.name == "intConst" -> "$blueColor id "
                    s.name == "doubleConst" -> "$blueColor id "
                    else -> "$whiteColor ${s.name} "
                }
            } else {
                "$violetColor ${s.name} "
            }
        return str
    }


}