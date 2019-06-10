package tableSymbol

import lexer.Token
import lexer.TokenType

class SymbolTableBuilder {

    private val symbolTable = TableSymbol<String>()

    private var isCreateValue = false
    private var isCreateNode = false
    private var isReadValue = false

    private var name: String? = null
    private var properties: Properties? = null
    private var type: String? = null

    private var nameNode: String? = null

    private var nameRead: String? = null

    /**
     * Формирование таблицы символов
     */
    fun addVariableSymbolTable(token: Token) {
        val tokenType = token.tokenType
        when {
            isReadValue -> when (tokenType) {
                TokenType.Equal, TokenType.Less, TokenType.Greater, TokenType.NotEqual,
                TokenType.CloseBrace, TokenType.EqualEqual, TokenType.Divide, TokenType.Multiply,
                TokenType.NewLine, TokenType.Comma, TokenType.Plus, TokenType.Minus -> {
                    if (nameRead != null) {
                        symbolTable.getValue(nameRead!!, token)
                        isReadValue = false
                        nameRead = null
                    }
                }
                TokenType.WhiteSpace -> { }
                else -> {
                    isReadValue = false
                    nameRead = null
                    analyzeToken(token)
                }
            }
            isCreateValue -> when (tokenType) {
                // id
                TokenType.Identifier -> {
                    if (name == null) {
                        name = token.tokenString
                    } else if (type == null) {
                        addVariable(token.tokenString, token)
                    }
                }
                // =
                TokenType.Equal -> {
                    addVariable(null, token)
                }
                // Double
                TokenType.Double, TokenType.Int -> {
                    addVariable(token.tokenString, token)
                }
                // ':', ' '
                TokenType.Extends, TokenType.WhiteSpace -> { }
                else -> {
                    isCreateValue = false
                    name = null
                    properties = null
                    type = null
                    nameNode = null
                }
            }
            isCreateNode -> when (tokenType) {
                // ( - аргументы класса или функции
                TokenType.OpenBrace, TokenType.Comma -> {
                    isCreateValue = true
                }
                // {
                TokenType.OpeningCurlyBrace -> {
                    nameNode = null
                    isCreateNode = false
                }
                // id
                TokenType.Identifier -> {
                    if (nameNode == null) {
                        nameNode = token.tokenString
                        symbolTable.createNode(nameNode!!)
                    }
                }
                // (i: Int, d: Double) - игнорируем
                TokenType.WhiteSpace, TokenType.CloseBrace, TokenType.Extends, TokenType.Int, TokenType.Double -> { }
                else -> {
                    isCreateNode = false
                    nameNode = null
                }
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
                isCreateNode()
            }
            // fun
            TokenType.Function -> {
                isCreateNode()
            }
            // {
            TokenType.OpeningCurlyBrace -> {
                if(!isCreateNode) {
                    symbolTable.createNode("block")
                    nameNode = null
                    isCreateNode = false
                }
            }
            // }
            TokenType.ClosingCurlyBrace -> {
                symbolTable.previousTreeNode()
            }
            else -> { }
        }
    }

    private fun addVariable(type: String?, token: Token){
        if (name != null) {
            symbolTable.addVariable(name!!, properties ?: Properties.Val, type ?: "None", "", token)
            isCreateValue = false
            name = null
            properties = null
        }
    }

    private fun isCreateValue() {
        isCreateValue = true
        isCreateNode = false
        isReadValue = false
    }

    private fun isCreateNode() {
        isCreateValue = false
        isCreateNode = true
        isReadValue = false
    }

    private fun isReadValue() {
        isCreateValue = false
        isCreateNode = false
        isReadValue = true
    }

    fun show() = symbolTable.showTreeNode()

}