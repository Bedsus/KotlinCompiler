import java.io.FileReader
import java.io.IOException

fun main() {
    val linesCodes = getLinesReadFiles("test.kt")
    val compiler = Compiler(linesCodes)
    compiler.start()
 //   compiler.showTokens()
 //   compiler.showRules()
 //   compiler.showSymbolTable()
}

/**
 * Считываем файл
 * @param file указаный файл
 * @return массив строк считанного файла
 */
private fun getLinesReadFiles(file: String): List<LinesCode> {
    val lines = mutableListOf<LinesCode>()
    var position = 1
    try {
        FileReader(file).use { reader ->
            var c: Int = reader.read()
            var temp = ""
            do {
                val ch = c.toChar()
                if (ch == '\n') {
                    if (temp.isNotEmpty()) {
                        lines.add(LinesCode(position, temp))
                        temp = ""
                        position++
                    }
                } else {
                    temp += ch
                }
                c = reader.read()
            } while (c != -1)
        }
    } catch (ex: IOException) {
        println(ex.message)
    }
    return lines
}

class LinesCode(var position: Int, var line: String)

val defaultColor = 27.toChar() + "[29m"
val whiteColor = 27.toChar() + "[30m"
val violetColor = 27.toChar() + "[35m"
val yellowColor = 27.toChar() + "[33m"
val blueColor = 27.toChar() + "[34m"
val redColor = 27.toChar() + "[32m"