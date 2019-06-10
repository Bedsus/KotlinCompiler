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


    inner class CreateSymbolTable {

        private val symbolTable = TableSymbol<String>()
        private var isCreateValue = false
        private var isEditValue = false

        private var name: String? = null
        private var properties: Properties? = null
        private var type: Type? = null
        private var value: String? = null

        /**
         * Формирование таблицы символов
         */
        fun addVariableSymbolTable(token: Token) {
            when (token.tokenType) {
                // {
                TokenType.OpeningCurlyBrace -> {
                    symbolTable.createNode()
                }
                // val
                TokenType.Value -> {
                    if (isCreateValue)
                        throw IllegalArgumentException("Попытка создать переменную во время создания другой")
                    isCreateValue = true
                    properties = Properties.Val
                }
                // var
                TokenType.Variable -> {
                    if (isCreateValue)
                        throw IllegalArgumentException("Попытка создать переменную во время создания другой")
                    isCreateValue = true
                    properties = Properties.Var
                }
                // id
                TokenType.Identifier -> {
                    if (!isEditValue && !isCreateValue) {
                        isEditValue = true
                    }
                    if (isCreateValue || isEditValue) {
                        if (name == null) {
                            name = token.tokenString
                            isEditValue = false
                        } else {
                            if (type != null && value == null) {
                                value = token.tokenString
                                /*if (type == Type.Int) {
                                    try {
                                        value = token.tokenString
                                    }catch (e: NumberFormatException){
                                        value = 0.0
                                    }
                                } else if (type == Type.Double) {
                                    try {
                                        value = token.tokenString.toDouble()
                                    }catch (e: NumberFormatException){
                                        value = 0.0
                                    }
                                }*/
                                if (isCreateValue && name != null && properties != null && type != null && value != null) {
                                    symbolTable.addVariable(name!!, properties!!, type!!, value!!)
                                    isCreateValue = false
                                    name = null
                                    properties = null
                                    type = null
                                    value = null
                                }
                                if (isEditValue && name != null && value != null) {
                                    symbolTable.updateVariable(name!!, value!!)
                                    isEditValue = false
                                    name = null
                                    value = null
                                }
                            }
                        }

                    }
                }
                // doubleConst
                TokenType.DoubleConstant, TokenType.IntConstant -> {
                    if (isEditValue && name != null) {
                        value = token.tokenString
                    }
                    if (type != null && value == null) {
                        value = token.tokenString
                       /* if (type == Type.Int) {
                            try {
                                value = token.tokenString.toDouble()
                            }catch (e: NumberFormatException){
                                value = 0.0
                            }
                        } else if (type == Type.Double) {
                            try {
                                value = token.tokenString.toDouble()
                            }catch (e: NumberFormatException){
                                value = 0.0
                            }
                        }*/
                    }

                    if (isCreateValue && name != null && properties != null && type != null && value != null) {
                        symbolTable.addVariable(name!!, properties!!, type!!, value!!)
                        isCreateValue = false
                        name = null
                        properties = null
                        type = null
                        value = null
                    }
                    if (isEditValue && name != null && value != null) {
                        symbolTable.updateVariable(name!!, value!!)
                        isEditValue = false
                        name = null
                        value = null
                    }
                }
                // :
                TokenType.Extends -> {
                    if (isCreateValue && name == null) {
                        throw IllegalArgumentException("Ошибка создания таблицы (:)")
                    }
                }
                // Double
                TokenType.Double -> {
                    if (isCreateValue && name == null) {
                        throw IllegalArgumentException("Ошибка создания таблицы (Double)")
                    }
                    if (isCreateValue || isEditValue)
                        type = Type.Double
                }
                // Int
                TokenType.Int -> {
                    if (isCreateValue && name == null) {
                        throw IllegalArgumentException("Ошибка создания таблицы (Double)")
                    }
                    if (isCreateValue || isEditValue)
                        type = Type.Int
                }
                // =
                TokenType.Equal -> {
                    if ((isCreateValue || isEditValue) && name == null) {
                        throw IllegalArgumentException("Ошибка создания таблицы (=)")
                    }
                    if (name != null && (!isCreateValue || !isEditValue)) {
                        isEditValue = true
                    }
                }
                // }
                TokenType.ClosingCurlyBrace  -> {
                    symbolTable.previousTreeNode()
                }
                TokenType.WhiteSpace -> {
                    //
                }
                else -> {
                    isCreateValue = false
                    isEditValue = false
                    name = null
                    properties = null
                    type = null
                    value = null
                }
            }
        }

        fun show() = symbolTable.showTreeNode()

    }

}