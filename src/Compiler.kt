import lexer.Lexer
import parser.Parser
import tableSymbol.SymbolTableBuilder
import java.io.File
import java.io.FileNotFoundException

/**
 * Компилятор языка Java
 * Выполняет все этапы компиляции кода:
 * 1. Лексический анализ `Lexer`
 * 2. Синтаксический анализ 'Parser'
 * 3. Семантический анализ
 * ...
 */
internal class Compiler(
    /**
     * Исходный код компилируемой программы
     */
    private val sourceCode: List<LinesCode>
) {

    private val lexer = Lexer()
    private val parser = Parser()
    private val symbolTable = SymbolTableBuilder()

    /**
     * запускаем процесс компиляции
     */
    fun start() {
        lexer()
        parser()
        symbolTable()
    }

    /**
     * Создание таблицы символов
     */
    private fun symbolTable(){
        for (token in lexer.tokens) {
            symbolTable.addVariableSymbolTable(token)
        }
    }

    /**
     * Запускаем лексер `lexer.Lexer`, а именно процесс формирования токенов
     */
    private fun lexer() {
        try {
            lexer.tokenize(sourceCode)
        } catch (e: AnalyzerException) {
            e.printStackTrace()
        }

    }

    /**
     * Запускаем парсер `Parser`, а именно процесс формирования графа по грамматике
     */
    private fun parser() {
        val grammarFile = File(System.getProperty("user.dir") + "/info/grammar.txt")
        try {
            parser.parse(grammarFile, lexer.filteredTokens)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: AnalyzerException) {
            e.printStackTrace()
        }

    }

    /**
     *
     * @return оисание токенов
     */
    /**
     * Получение всех токенов в "читаемой" форме
     * @param isAll выводить ли все токены, или только основные
     * @return оисание токенов
     */
    private fun getTokens(isAll: Boolean): String {
        val str = StringBuilder()
        var i = 0
        for (token in lexer.tokens) {
            if (token.tokenType.isAuxiliary) {
                if (isAll) {
                    str.append("   ")
                        .append(token.toString())
                        .append("\n")
                }
            } else {
                i++
                str.append(i)
                    .append("   ")
                    .append(token.toString())
                    .append("\n")
            }
        }
        return str.toString()
    }

    private fun getRules(): String {
        val stringBuilder = StringBuilder()
        for (r in parser.getSequenceOfAppliedRules()) {
            stringBuilder.append(r.toString())
                .append("\n")
        }
        return stringBuilder.toString()
    }

    fun showTokens(){
        println("""$redColor
|----------------------------------|
|            L E X E R             |
|----------------------------------|
        $defaultColor""".trimIndent())
        println(getTokens(false))
    }

    fun showRules(){
        println("""$redColor
|----------------------------------|
|           P A R S E R            |
|----------------------------------|
     $defaultColor""".trimIndent())
       println(getRules())
    }

    fun showSymbolTable(){
        println("""$redColor
|----------------------------------|
|     S Y M B O L   T A B L E      |
|----------------------------------|
        $defaultColor""".trimIndent())
        println(symbolTable.show())
    }
}
