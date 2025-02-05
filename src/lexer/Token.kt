package lexer

import blueColor
import defaultColor
import violetColor
import whiteColor
import java.util.*

/**
 * Класс представляет токен (лексему).
 * Токен - это строка (набор символов), классифицированая по указаным правилам.
 * Например: Идентификатор, запятая, двойная константа.
 *
 * @param line строка в коде
 * @param column столбец в коде
 * @param end конечный индекс токена на входе
 * @param tokenString строка символов для этого токена
 * @param tokenType тип, связанный с этим токеном
 */
class Token(
    val line: Int,
    val column: Int,
    val end: Int,
    val tokenString: String,
    val tokenType: TokenType
) {

    fun position() = "[$column,$line]"

    override fun toString() = if (!this.tokenType.isAuxiliary)
        "$blueColor[$column,$line]$defaultColor $violetColor$tokenType$defaultColor " +
                "'$whiteColor$tokenString$defaultColor'" else "[$column,$line] $tokenType"
}


/**
 * Перечисление `TokeType` представляет типы токенов в подмножестве языка Java
 *
 */
enum class TokenType {

    BlockCommentNew, BlockCommentEnd, LineComment, SingleQuote, WhiteSpace, Tab, NewLine, CloseBrace, OpenBrace, Value, Variable,
    Class, Function, Int, Char, ArrayInt, ArrayChar, OpeningCurlyBrace, ClosingCurlyBrace, OpeningSquareBrace, ClosingSquareBrace,
    CharConstant, IntConstant, Plus, Minus, Multiply, CommentLine, Divide, Point, EqualEqual, Equal, NotEqual, Public, Private,  False,
    True, Null, Return, Extends, If, While, Else, Comma, EndLine, Greater, Array, Less, Identifier;

    /**
     * Определяет, является ли этот токен вспомогательным
     *
     * @return `true`, если токен является вспомогательным, `false` в противном случае
     */
    val isAuxiliary: Boolean
        get() = (this == BlockCommentNew || this == BlockCommentEnd || this == LineComment
                || this == NewLine || this == Tab || this == WhiteSpace || this == EndLine)

    val isCompiler: Boolean
        get() = (
            this == Return || this == CharConstant || this == IntConstant || this == If || this == Else ||
            this == While || this == OpenBrace || this == CloseBrace || this == OpeningCurlyBrace ||
            this == ClosingCurlyBrace || this == OpeningSquareBrace || this == ClosingSquareBrace ||
            this == Comma || this == Plus || this == Minus || this == Multiply || this == Divide ||
            this == EqualEqual || this == Equal || this == NotEqual ||  this == Greater || this == Less ||
            this == Identifier || this == Function || this == Class
                )
}

class RegExToken {
    /** Сопоставление типа токена с его регулярным выражением  */
    private var regEx = TreeMap<TokenType, String>()

    init{
        createRegEx()
    }

    fun get(tokenType: TokenType) = regEx[tokenType]

    /**
     * Создает карту из типов токена в его регулярные выражения
     *
     */
    private fun createRegEx() {
        // Игнорируем
        regEx[TokenType.BlockCommentNew] = "(\\/\\*).*"
        regEx[TokenType.BlockCommentEnd] = "(\\*?\\*\\/).*"
        regEx[TokenType.LineComment] = "(//(.*?)).*"
        regEx[TokenType.WhiteSpace] = "( ).*"
        regEx[TokenType.Tab] = "(\\t).*"
        regEx[TokenType.EndLine] = "(\\r).*"
        regEx[TokenType.NewLine] = "(\\n).*"
        // Фиксируем
        regEx[TokenType.Int] = "\\b(Int)\\b.*"
        regEx[TokenType.Char] = "\\b(Char)\\b.*"
        regEx[TokenType.ArrayInt] = "(Array<Int>).*"
        regEx[TokenType.ArrayChar] = "(Array<Char>).*"
        regEx[TokenType.Array] = "(Array).*"
        regEx[TokenType.Value] = "\\b(val)\\b.*"
        regEx[TokenType.Variable] = "\\b(var)\\b.*"
        regEx[TokenType.Class] = "\\b(class)\\b.*"
        regEx[TokenType.Function] = "\\b(fun)\\b.*"
        regEx[TokenType.Return] = "\\b(return)\\b.*"
        regEx[TokenType.Public] = "\\b(public)\\b.*"
        regEx[TokenType.Private] = "\\b(private)\\b.*"
        regEx[TokenType.CharConstant] = "('([a-zA-Z]{1})').*"
        regEx[TokenType.IntConstant] = "\\b(\\d{1,9})\\b.*"
        regEx[TokenType.If] = "\\b(if)\\b.*"
        regEx[TokenType.Else] = "\\b(else)\\b.*"
        regEx[TokenType.While] = "\\b(while)\\b.*"
        regEx[TokenType.False] = "\\b(false)\\b.*"
        regEx[TokenType.True] = "\\b(true)\\b.*"
        regEx[TokenType.OpenBrace] = "(\\().*"
        regEx[TokenType.CloseBrace] = "(\\)).*"
        regEx[TokenType.OpeningCurlyBrace] = "(\\{).*"
        regEx[TokenType.ClosingCurlyBrace] = "(\\}).*"
        regEx[TokenType.OpeningSquareBrace] = "(\\[).*"
        regEx[TokenType.ClosingSquareBrace] = "(\\]).*"
        regEx[TokenType.Extends] = "(:).*"
        regEx[TokenType.Comma] = "(,).*"
        regEx[TokenType.Point] = "(\\.).*"
        regEx[TokenType.Plus] = "(\\+{1}).*"
        regEx[TokenType.Minus] = "(\\-{1}).*"
        regEx[TokenType.Multiply] = "(\\*).*"
        regEx[TokenType.Divide] = "(/).*"
        regEx[TokenType.EqualEqual] = "(==).*"
        regEx[TokenType.Equal] = "(=).*"
        regEx[TokenType.NotEqual] = "(\\!=).*"
        regEx[TokenType.Greater] = "(>).*"
        regEx[TokenType.Less] = "(<).*"
        regEx[TokenType.Identifier] = "\\b([a-zA-Z]{1}[0-9a-zA-Z_]{0,31})\\b.*"
    }
}