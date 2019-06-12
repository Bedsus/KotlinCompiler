package parser

import lexer.Token
import lexer.TokenType

class TokenAnalyzer(private val tokens: List<Token>) {

    val functionToken = mutableMapOf<String, MutableList<Token>>()

    val classToken = mutableListOf<Token>()
    var className : String? = null

    var currentFunction: String = ""

    var tokenLine = mutableListOf<Token>()


    var numberToken = 0

    /**
     * Уровень вложенности
     */
    private var level = 0

    fun refactorTokens() {
        for (token in tokens) {
            analyzerFunction(token)
            numberToken++
        }
        functionToken.put(className!!, classToken)
    }

    var ignoreLine = 0

    private fun analyzerFunction(token: Token) {
        var type: TypeCode? = null
        when (token.tokenType) {
            TokenType.Class -> {
                ignoreLine = token.column
            }
            TokenType.Identifier -> {
                if(className == null){
                    className = token.tokenString
                }
            }
            TokenType.Function -> {
                ignoreLine = token.column
                currentFunction = tokens[numberToken + 1].tokenString
            }
            TokenType.ClosingCurlyBrace -> {
                level--
                if (level == 1) {
                    functionToken.put(currentFunction, tokenLine)
                    tokenLine = mutableListOf()

                }
                if(level < 2){
                    ignoreLine = token.column
                }
            }
            TokenType.OpeningCurlyBrace -> {
                level++
            }
            else -> {

            }
        }
        if (ignoreLine != token.column) {
            if (level > 1) {
                tokenLine.add(token)
            } else {
                classToken.add(token)
            }
        }
    }

    fun showFunction() : String {
        var s = ""
        for((key, values) in functionToken) {
            s += key + '\n'
            for(value in values){
                s += value.toString() + '\n'
            }
        }
        return s
    }
}

class CodeLine(val type: TypeCode, val list: MutableList<Token>)

enum class TypeCode {
    Fun, Assign, BooleanOp, While, If, Else, Close;
}