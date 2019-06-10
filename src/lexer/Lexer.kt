package lexer

import LinesCode
import exceptions.AnalyzerException
import tableSymbol.Properties
import tableSymbol.TableSymbol
import tableSymbol.Type
import java.util.*
import java.util.regex.Pattern

/**
 * Класс `Lexer` представляет лексический анализатор для языка Java
 */
internal class Lexer {

    private val symbolTable = CreateSymbolTable()

    /** Сопоставление типа токена с его регулярным выражением  */
    private val regEx = RegExToken()

    /** Список токенов, которые появляются во входном источнике  */
    val tokens = mutableListOf<Token>()

    /** Определяет режим комментария, который игнорирует строку коментария  */
    private var isCommentLineMode = false

    /** Определяет режим комментария, который игнорирует блок коментария  */
    private var isCommentBlockMode = false

    val filteredTokens: MutableList<Token>
        get() {
            val filteredResult = ArrayList<Token>()
            for (t in this.tokens) {
                if (!t.tokenType.isAuxiliary) {
                    filteredResult.add(t)
                }
            }
            return filteredResult
        }

    /**
     * Выполняет токенизацию входного исходного кода.
     *
     * @param source строка для анализа
     * @throws AnalyzerException если в источнике существует лексическая ошибка
     */
    @Throws(AnalyzerException::class)
    fun tokenize(source: List<LinesCode>) {
        for (line in source) {
            val code = line.line
            var position = 0
            var token: Token?
            isCommentLineMode = false
            do {
                token = separateToken(code, position, line.position)
                if (token != null) {
                    position = token.end
                    tokens.add(token)
                    symbolTable.addVariableSymbolTable(token)
                }
            } while (token != null && position != code.length)
            if (position != code.length) {
                throw AnalyzerException("Лексическая ошибка на позиции # $position")
            }
        }
    }

    /**
     * Сканирует источник по определенному индексу и возвращает первый разделенный токен
     *
     * @param source исходный код для сканирования
     * @param fromIndex индекс, по которому начинается сканирование
     * @return первый разделенный токен или `null`, если токен не был найден
     */
    private fun separateToken(source: String, fromIndex: Int, column: Int): Token? {
        if (fromIndex < 0 || fromIndex >= source.length) {
            throw IllegalArgumentException("Неверный индекс!")
        }
        for (tokenType in TokenType.values()) {
            val p = Pattern.compile(
                ".{" + fromIndex + "}" + regEx.get(tokenType),
                Pattern.DOTALL
            )
            val m = p.matcher(source)
            if (m.matches()) {
                if (tokenType == TokenType.LineComment) {
                    isCommentLineMode = true
                }
                if (tokenType == TokenType.BlockCommentNew) {
                    isCommentBlockMode = true
                } else if (tokenType == TokenType.BlockCommentEnd && isCommentBlockMode) {
                    isCommentBlockMode = false
                }
                var currentTokenType = tokenType
                if (isCommentLineMode || isCommentBlockMode) {
                    currentTokenType = TokenType.BlockCommentNew
                }
                val lex = m.group(1)
                return Token(fromIndex + 1, column, fromIndex + lex.length, lex, currentTokenType)
            }
        }
        return null
    }

    fun showTable() = symbolTable.show()

    /**
     * Фориморвание таблицы символов
     * Режимы:
     * 1. Добавление нового узла
     * 2. Добавление переменой
     */
    inner class CreateSymbolTable {

        private val symbolTable = TableSymbol<String>()
        private var nameNode: String? = null
        private var isCreateValue = false
        private var isCreateNode = false

        private var name: String? = null
        private var properties: Properties? = null
        private var type: Type? = null

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
                        if(name == null)
                            name = token.tokenString
                    }
                    // =
                    TokenType.Equal -> {
                        if (name != null && properties != null) {
                            symbolTable.addVariable(name!!, properties!!, type ?: Type.None, "")
                            isCreateValue = false
                            name = null
                            properties = null
                            type = null
                        }
                    }
                    // Double
                    TokenType.Double -> {
                        type = Type.Double
                    }
                    // Int
                    TokenType.Int -> {
                        type = Type.Int
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

        fun isCreateValue() {
            isCreateValue = true
            isCreateNode = false
        }

        fun isCreateNode() {
            isCreateValue = false
            isCreateNode = true
        }

        fun show() = symbolTable.showTreeNode()

    }

}