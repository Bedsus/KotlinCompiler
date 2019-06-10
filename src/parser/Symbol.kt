package parser

/**
 * Класс представляет собой терминальный символ грамматики
 */
data class Terminal(override val code: Int, override val name: String) : Symbol()

/**
 * Класс представляет нетерминальный символ грамматики
 */
data class NonTerminal(override val code: Int, override val name: String) : Symbol()

/**
 * Класс представляет собой символ грамматики
 */
abstract class Symbol {

    /**
     * Код символа
     */
    abstract val code: Int
    /**
     * Обозначения в грамматике
     */
    abstract val name: String

    override fun toString() = name

    override fun hashCode() = code

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        other as Terminal
        if (code != other.code) return false
        return true
    }
}