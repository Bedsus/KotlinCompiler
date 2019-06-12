package tableSymbol

import lexer.Token
import lexer.TokenType

class SymbolTableBuilder {

    private val symbolTable = TableSymbol<String>()

    /**
     * Режим записи новой переменной
     */
    private var isCreateValue = false
    /**
     * Режим создания нового узла
     */
    private var isCreateFunNode = false


    private var isCreateClassNode = false
    /**
     * Режим чтения переменной
     */
    private var isReadValue = false

    private var name: String? = null
    private var properties: Properties? = null

    private var nameRead: String? = null

    /**
     * Формирование таблицы символов
     *
     * @param token проверяемый токен
     */
    fun addVariableSymbolTable(token: Token) {
        val tokenType = token.tokenType
        when {
            isReadValue -> when (tokenType) {
                TokenType.OpenBrace, TokenType.Extends, TokenType.Point -> {
                    isReadValue = false
                    nameRead = null
                    analyzeToken(token)
                }
                else -> {
                    if (nameRead != null) {
                        symbolTable.getValue(nameRead!!, token)
                        isReadValue = false
                        nameRead = null
                    }
                    analyzeToken(token)
                }
            }
            isCreateValue -> when (tokenType) {
                TokenType.CloseBrace -> {
                    isCreateFunNode = false
                    isCreateValue = false
                }
                // id
                TokenType.Identifier -> {
                    if (name == null) {
                        name = token.tokenString
                    }
                }
                // Type
                TokenType.Char, TokenType.Int, TokenType.ArrayChar, TokenType.ArrayInt -> {
                    if (name != null) {
                        symbolTable.addVariable(name!!, properties ?: Properties.Val, token.tokenType, "", token)
                        isCreateValue = false
                        name = null
                        properties = null
                    }
                }
                else -> { }
            }
            isCreateClassNode -> when (tokenType) {
                // id
                TokenType.Identifier -> {
                    symbolTable.createNode(token.tokenString)
                }
                TokenType.OpeningCurlyBrace -> {
                    isCreateClassNode = false
                }
                else -> { }
            }
            isCreateFunNode -> when (tokenType) {
                TokenType.CloseBrace -> {
                    isCreateFunNode = false
                }
                // id
                TokenType.Identifier -> {
                    symbolTable.createNode(token.tokenString)
                }
                TokenType.OpenBrace, TokenType.Comma -> {
                    isCreateValue = true
                }
                TokenType.OpeningCurlyBrace -> {
                    symbolTable.createNode("block")
                }
                else -> { }
            }
            else -> analyzeToken(token)
        }
    }

    private fun analyzeToken(token: Token) {
        when (token.tokenType) {
            // id
            TokenType.Identifier -> {
                isReadValue()
                if (nameRead == null) {
                    nameRead = token.tokenString
                }
            }
            // val
            TokenType.Value -> {
                isCreateValue()
                properties = Properties.Val
            }
            // var
            TokenType.Variable -> {
                isCreateValue()
                properties = Properties.Var
            }
            // class
            TokenType.Class -> {
                isCreateClassNode = true
            }
            // fun
            TokenType.Function -> {
                isCreateNode()
            }
            // {
            TokenType.OpeningCurlyBrace -> {
                symbolTable.createNode("block")
            }
            // }
            TokenType.ClosingCurlyBrace -> {
                symbolTable.previousTreeNode()
            }
            else -> { }
        }
    }

    private fun isCreateValue() {
        isCreateValue = true
        isCreateFunNode = false
        isReadValue = false
    }

    private fun isCreateNode() {
        isCreateValue = false
        isCreateFunNode = true
        isReadValue = false
    }

    private fun isReadValue() {
        isCreateValue = false
        isCreateFunNode = false
        isReadValue = true
    }

    fun show() = symbolTable.showTreeNode()

}