package parser

import lexer.Token
import lexer.TokenType
import tableSymbol.TableSymbol

class TokenAnalyzer(private val tokens: List<Token>, val tableSymbol: TableSymbol<String>) {

    val functionToken = mutableMapOf<String, MutableList<Token>>()

    val classTokens = mutableListOf<Token>()
    var className : String? = null

    var currentFunction: String = ""

    var tokenLine = mutableListOf<Token>()

    //var result:

    var numberToken = 0

    /**
     * Уровень вложенности
     */
    private var level = 0


    private var isFuncPlay = false

    fun refactorTokens() {
        for (token in tokens) {
            analyzerFunction(token)
            numberToken++
        }
        tableSymbol.setCurrentNode(tokens[numberToken + 1].tokenString)
        for(classToken in classTokens){

        }

    }

    var ignoreLine = 0

    /**
     * Отделяем функции
     */
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
                // Вызов функции
                if(tokens[numberToken + 1].tokenType == TokenType.OpenBrace &&
                    tokens[numberToken - 1].tokenType != TokenType.Function) {
                    tableSymbol.setCurrentNode(currentFunction)
                    isFuncPlay = true
                }
            }
            TokenType.Function -> {
                ignoreLine = token.column
                currentFunction = tokens[numberToken + 1].tokenString
                tableSymbol.setCurrentNode(currentFunction)
               // println(tableSymbol.showCurrentValue())
            }
            TokenType.ClosingCurlyBrace -> {
                level--
                if (level == 1) {
                    functionToken.put(currentFunction, tokenLine)
                    tableSymbol.previous()
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
                classTokens.add(token)
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

class Action (val actionType: TokenType, val token1: Token, val token2: Token)

class CodeLine(val type: TypeCode, val list: MutableList<Token>)

enum class TypeCode {
    Assign, While, If;
}