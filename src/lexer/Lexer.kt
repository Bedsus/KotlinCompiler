package lexer

import AnalyzerException
import LinesCode
import java.util.*
import java.util.regex.Pattern

/**
 * Класс `Lexer` представляет лексический анализатор для языка Java
 */
internal class Lexer {

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

}