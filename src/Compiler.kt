import lexer.Lexer
import lexer.Token
import parser.Parser
import parser.TokenAnalyzer
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

        val tokens = mutableListOf<Token>()
        for (token in lexer.tokens) {
            if (token.tokenType.isCompiler) {
                tokens.add(token)
            }
        }
        val tokenAnalyzer = TokenAnalyzer(tokens)
        tokenAnalyzer.refactorTokens()
        println(tokenAnalyzer.showFunction())
    }

    /**
     * Создание таблицы символов
     */
    private fun symbolTable(){
        for (token in lexer.tokens) {
            if(!token.tokenType.isAuxiliary) {
                symbolTable.addVariableSymbolTable(token)
            }
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
     * @return оисание токенов
     */
    private fun getTokens(): String {
        val str = StringBuilder()
        for (token in lexer.tokens) {
            if (token.tokenType.isCompiler) {
                str.append("   ")
                    .append(token.tokenString)
                    .append("   \n")
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
        println(getTokens())
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

/**
 * Класс `AnalyzerException` представляет исключения, которые могут быть вызваны
 * по лексическим или синтаксическим ошибкам
 *
 * Положение во входном источнике (лексере) или номер токена (парсера), где
 * произошла ошибка
 */
class AnalyzerException(override val message: String) : Exception()
