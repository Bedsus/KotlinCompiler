package tableSymbol

import lexer.Token
import lexer.TokenType

class SymbolTableBuilder {

    private val symbolTable = TableSymbol<String>()
    private var nameNode: String? = null
    private var isCreateValue = false
    private var isCreateNode = false

    private var name: String? = null
    private var properties: Properties? = null
    private var type: String? = null

    /**
     * Формирование таблицы символов
     */
    fun addVariableSymbolTable(token: Token) {
        val tokenType = token.tokenType
        when {
            isCreateNode -> when (tokenType) {
                // {
                TokenType.OpeningCurlyBrace -> {
                    symbolTable.createNode(nameNode!!)
                    nameNode = null
                    isCreateNode = false
                }
                // id
                TokenType.Identifier -> {
                    if (nameNode == null) {
                        nameNode = token.tokenString
                    }
                }
                // (i: Int, d: Double) - игнорируем
                TokenType.WhiteSpace, TokenType.OpenBrace, TokenType.CloseBrace,
                TokenType.Extends, TokenType.Int, TokenType.Double, TokenType.Comma -> {

                }
                else -> {
                    isCreateNode = false
                    nameNode = null
                }
            }
            isCreateValue -> when (tokenType) {
                // id
                TokenType.Identifier -> {
                    if (name == null) {
                        name = token.tokenString
                    } else if (type == null) {
                        type = token.tokenString
                    }
                }
                // =
                TokenType.Equal -> {
                    if (name != null && properties != null) {
                        symbolTable.addVariable(name!!, properties!!, type ?: "None", "")
                        isCreateValue = false
                        name = null
                        properties = null
                        type = null
                    }
                }
                // Double
                TokenType.Double, TokenType.Int -> {
                    if (type == null) {
                        type = token.tokenString
                    }
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
            else -> when (tokenType) {
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
    }

    private fun isCreateValue() {
        isCreateValue = true
        isCreateNode = false
    }

    private fun isCreateNode() {
        isCreateValue = false
        isCreateNode = true
    }

    fun show() = symbolTable.showTreeNode()

}