package parser

import AnalyzerException
import lexer.Token
import lexer.TokenType
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import java.util.AbstractMap.SimpleEntry

/**
 * Класс `Parser` представляет собой интеллектуальный синтаксический анализатор.
 * Принимает только LL (1) грамматику. Если грамматика не LL (1), скорее всего, вы получите `StackOverflowError`.
 * Произведения в грамматике используют следующее формат, например:
 *
 * Цель -> А
 * A -> (A) | B
 * B -> а
 *
 * Символ выводится как нетерминальный первым заглавным символом. "->" обозначает
 * определение, "|" обозначает чередование, а новые строки обозначают окончание.
 * Используйте «EPSILON» для представления пустой строки. Поместите пробелы между вещами,
 * которые не нужно читать как один символ: (A)! = (A).
 */

class Parser {

    /** Терминальный символ грамматики, представляющий пустую строку  */
    private val epsilon = Terminal(0, "EPSILON")

    /** Терминальный символ, обозначающий конец программы  */
    private val endOfProgram = Terminal(-1, "END_OF_PROGRAM")

    /** Начальный символ грамматики  */
    private lateinit var startSymbol: NonTerminal

    /** Список правил в грамматике без изменений  */
    private val rules = mutableListOf<Rule>()

    /** Грамматический алфавит. Содержит терминальные и нетерминальные символы  */
    private val alphabet = mutableSetOf<Symbol>()

    /** Отображение из строкового представления символа в его объект  */
    private val nameToSymbol = mutableMapOf<String, Symbol>()

    /** Отображение от символа к его первому набору  */
    private val firstSet = mutableMapOf<Symbol, MutableSet<Terminal>>()

    /** Отображение от символа к его следующему набору  */
    private val followSet = mutableMapOf<Symbol, MutableSet<Terminal>>()

    /** Представление таблицы разбора для парсера LL (1)  */
    private val parsingTable = mutableMapOf<SimpleEntry<NonTerminal, Terminal>, Array<Symbol>>()

    /** Стек терминалов, которые были построены из входных токенов  */
    private lateinit var input: Stack<Terminal>

    /** Последовательность применяемых правил при выводе  */
    private val sequenceOfAppliedRules = mutableListOf<Rule>()

    private lateinit var tokenList: List<Token>

    /**
     * Анализирует источник, представленный списком токенов, используя указанные LL (1) правила грамматики
     *
     * @param grammarFile файл с правилами грамматики
     * @param list список токенов с входа
     * @throws FileNotFoundException если файл не существует
     * @throws AnalyzerException если вход содержит синтаксическую ошибку
     */
    @Throws(FileNotFoundException::class, AnalyzerException::class)
    fun parse(grammarFile: File, list: MutableList<Token>) {
        alphabet.add(epsilon)
        parseRules(grammarFile)
        calculateFirst()
        calculateFollow()
        buildParsingTable()
        tokenList = list
        input = convertTokensToStack(list)
        performParsingAlgorithm()
    }

    /**
     * Возвращает последовательность правил грамматики, которые применялись во время разбора
     *
     * @return  список применяемых правил
     */
    fun getSequenceOfAppliedRules(): List<Rule> {
        return sequenceOfAppliedRules
    }

    /**
     * Реализует алгоритм LL (1) прогнозирующего анализа
     *
     * @throws AnalyzerException если найдена синтаксическая ошибка
     */
    @Throws(AnalyzerException::class)
    private fun performParsingAlgorithm() {
        val stack = Stack<Symbol>()
        stack.push(endOfProgram)
        stack.push(startSymbol)
        var parsedTokensCount = 0
        do {
            val stackTop = stack.peek()
            val inputTop = input.peek()
            if (stackTop is Terminal) {
                if (stackTop == inputTop) {
                    stack.pop()
                    input.pop()
                    parsedTokensCount++
                } else {
                    showAnalyzerException(parsedTokensCount)
                }
            } else {
                val tableKey : SimpleEntry<NonTerminal, Terminal> = SimpleEntry(stackTop as NonTerminal, inputTop)
                if (tableKey.value == endOfProgram) {
                    return
                }
                if (parsingTable.containsKey(tableKey)) {
                    stack.pop()
                    val tableEntry = parsingTable[tableKey]
                    for (j in tableEntry!!.size - 1 downTo -1 + 1) {
                        if (tableEntry[j] != epsilon)
                            stack.push(tableEntry[j])
                    }
                    sequenceOfAppliedRules.add(getRule(stackTop, tableEntry)!!)
                } else {
                    showAnalyzerException(parsedTokensCount)
                }
            }
        } while (!stack.isEmpty() && !input.isEmpty())
        if (!input.isEmpty()) {
            showAnalyzerException(parsedTokensCount)
        }
    }

    @Throws(AnalyzerException::class)
    private fun showAnalyzerException(parsedTokensCount: Int) {
        val position = tokenList.size - parsedTokensCount - 1 // т.к. массив инвертированный
        val token = tokenList[position]
        throw AnalyzerException("Синтаксическая ошибка на позиции " + token.tokenString + token.position())
    }

    /**
     * Преобразует список токенов из лексера в стек терминалов для парсера.
     * Первый токен на входе будет наверху стека.
     *
     * @param inputTokens список входных токенов
     * @return стек терминальных символов
     */
    private fun convertTokensToStack(inputTokens: MutableList<Token>): Stack<Terminal> {
        val input = Stack<Terminal>()
        inputTokens.reverse()
        input.push(endOfProgram)
        for (token in inputTokens) {
            var s = nameToSymbol[token.tokenString]
            if (s == null || s is NonTerminal) {
                s = when (token.tokenType) {
                    TokenType.Identifier -> nameToSymbol["id"] as Terminal
                    TokenType.IntConstant -> nameToSymbol["intConst"] as Terminal
                    TokenType.DoubleConstant -> nameToSymbol["doubleConst"] as Terminal
                    else -> throw RuntimeException("Something is wrong!")
                }
            }
            input.push(s as Terminal?)
        }
        return input
    }

    /**
     * Автоматически создает таблицу разбора LL (1), используя follow и first set
     */
    private fun buildParsingTable() {
        for (r in rules) {
            val rightSide = r.rightSide
            val leftSide = r.leftSide
            val firstSetForRightSide = first(rightSide)
            val followSetForLeftSide = followSet[leftSide]!!
            for (s in firstSetForRightSide) {
                parsingTable[SimpleEntry(leftSide, s)] = rightSide
            }
            if (firstSetForRightSide.contains(epsilon)) {
                for (s in followSetForLeftSide) {
                    parsingTable[SimpleEntry(leftSide, s)] = rightSide
                }
            }
        }
    }

    private fun calculateFirst() {
        for (s in alphabet) {
            firstSet[s] = HashSet()
        }
        for (s in alphabet) {
            first(s)
        }
    }

    /**
     * Рассчитывает первый набор для указанного символа. Используя следующие правила:
     *
     * 1. Если X является терминальным, тогда FIRST (X) равен {X}.
     * 2. Если X -> EPSILON - производство, то добавьте EPSILON в ПЕРВЫЙ (X).
     * 3. Если X нетерминально и X -> Y1, Y2 ... Yk - произведение,
     * затем поместите * a * i> (терминал) в FIRST (X), если для некоторого i * a * i> в FIRST (Yi), и Y1, ..., Yi-1 -> EPSILON.
     * Если EPSILON находится в FIRST (Yj) для всех j = 1, 2, ..., k, то добавьте EPSILON в FIRST (X).
     *
     * @param s терминальный или нетерминальный символ грамматики
     */
    private fun first(s: Symbol) {
        val first = firstSet[s]!!
        var auxiliarySet: MutableSet<Terminal>
        if (s is Terminal) {
            first.add(s)
            return
        }

        for (r in getRulesWithLeftSide(s as NonTerminal)) {
             val rightSide = r.rightSide
            /** Погружаемся для выявления терминального символа  */
            first(rightSide[0])
            auxiliarySet = HashSet(firstSet[rightSide[0]])
            auxiliarySet.remove(epsilon)
            first.addAll(auxiliarySet)

            var i = 1
            while (i < rightSide.size && firstSet[rightSide[i - 1]]!!.contains(epsilon)) {
                first(rightSide[i])
                auxiliarySet = HashSet(firstSet[rightSide[i]])
                auxiliarySet.remove(epsilon)
                first.addAll(auxiliarySet)
                i++
            }

            var allContainEpsilon = true
            for (rightS in rightSide) {
                if (!firstSet[rightS]!!.contains(epsilon)) {
                    allContainEpsilon = false
                    break
                }
            }
            if (allContainEpsilon)
                first.add(epsilon)
        }
    }

    /**
     * Рассчитывает первый набор для цепочки символов
     *
     * @param chain строка символов
     * @return первый набор для указанной строки
     */
    private fun first(chain: Array<Symbol>): MutableSet<Terminal> {
        var auxiliarySet = HashSet(firstSet[chain[0]])
        auxiliarySet.remove(epsilon)
        val firstSetForChain = HashSet(auxiliarySet)

        var i = 1
        while (i < chain.size && firstSet[chain[i - 1]]!!.contains(epsilon)) {
            auxiliarySet = HashSet(firstSet[chain[i]])
            auxiliarySet.remove(epsilon)
            firstSetForChain.addAll(auxiliarySet)
            i++
        }

        var allContainEpsilon = true
        for (s : Symbol in chain) {
            if (!firstSet[s]!!.contains(epsilon)) {
                allContainEpsilon = false
                break
            }
        }
        if (allContainEpsilon)
            firstSetForChain.add(epsilon)

        return firstSetForChain
    }

    private fun calculateFollow() {
        for (s in alphabet) {
            if (s is NonTerminal)
                followSet[s] = HashSet()
        }

        val callTable = HashMap<SimpleEntry<Symbol, Symbol>, Boolean>()
        for (firstS in alphabet) {
            for (secondS in alphabet) {
                callTable[SimpleEntry(firstS, secondS)] = false
            }
        }

        val firstSymbol = rules[0].leftSide
        followSet[firstSymbol]!!.add(endOfProgram)
        for (s in alphabet) {
            if (s is NonTerminal) {
                follow(s, null, callTable)
            }
        }
    }

    /**
     * Рассчитывает следующий набор для нетерминальных символов
     */
    private fun follow(
        s: NonTerminal, caller: Symbol?,
        callTable: MutableMap<SimpleEntry<Symbol, Symbol>, Boolean>
    ) {
        val called = callTable[SimpleEntry<Symbol, Symbol>(caller, s)]
        if (called != null) {
            if (called)
                return
            else
                callTable[SimpleEntry<Symbol, Symbol>(caller, s)] = true
        }

        val follow = followSet[s]!!
        var auxiliarySet: MutableSet<Terminal>

        val list = getLeftSideRightChain(s)
        for ((leftSide, rightChain) in list) {
            if (rightChain.isNotEmpty()) {
                auxiliarySet = first(rightChain)
                auxiliarySet.remove(epsilon)
                follow.addAll(auxiliarySet)
                if (first(rightChain).contains(epsilon)) {
                    follow(leftSide, s, callTable)
                    follow.addAll(followSet[leftSide]!!)
                }
            } else {
                follow(leftSide, s, callTable)
                follow.addAll(followSet[leftSide]!!)
            }
        }
    }

    /**
     * Создает грамматические правила из файла
     *
     * @param grammarFile файл с правилами грамматики
     * @throws FileNotFoundException если файл с указанным путем не существует
     */
    @Throws(FileNotFoundException::class)
    private fun parseRules(grammarFile: File) {
        nameToSymbol["EPSILON"] = epsilon

        val data = Scanner(grammarFile)
        var code = 1
        var ruleNumber = 0
        while (data.hasNext()) {
            val t = StringTokenizer(data.nextLine())
            var symbolName = t.nextToken()
            if (!nameToSymbol.containsKey(symbolName)) {
                val s = NonTerminal(code, symbolName)
                if (code == 1)
                    startSymbol = s
                nameToSymbol[symbolName] = s
                alphabet.add(s)
                code++
            }
            t.nextToken()// ->

            val leftSide = nameToSymbol[symbolName] as NonTerminal
            while (t.hasMoreTokens()) {
                val rightSide = ArrayList<Symbol>()
                do {
                    symbolName = t.nextToken()
                    if (symbolName != "|") {
                        if (!nameToSymbol.containsKey(symbolName)) {
                            /**
                             * Считываение нетерминальных символов (начинаются с заглавной буквы)
                             * Исключения это Int и Double
                             */
                            val s: Symbol =
                                if (Character.isUpperCase(symbolName[0]))
                                    if(symbolName == "Int" || symbolName == "Double")
                                        Terminal(code++, symbolName)
                                    else
                                        NonTerminal(code++, symbolName)
                                else
                                    Terminal(code++, symbolName)
                            nameToSymbol[symbolName] = s
                            alphabet.add(s)
                        }
                        rightSide.add(nameToSymbol[symbolName]!!)
                    }
                } while (symbolName != "|" && t.hasMoreTokens())
                rules.add(Rule(ruleNumber++, leftSide, rightSide.toTypedArray()))
            }
        }
    }

    /**
     * Возвращает правила с указанной левой стороной
     *
     * @param nonTerminalSymbol символ в левой части производства
     * @return набор правил, которые содержат указанный символ в левой части
     */
    private fun getRulesWithLeftSide(nonTerminalSymbol: NonTerminal): Set<Rule> {
        val set = mutableSetOf<Rule>()
        for (r in rules) {
            if (r.leftSide == nonTerminalSymbol)
                set.add(r)
        }
        return set
    }

    /**
     * Возвращает список пар. Первый элемент пары является левой стороной
     * правило, если это правило содержит указанный символ `s` справа.
     * Второй элемент содержит символы после `s` в правой части
     * правило.
     *
     * @param s проверяемый символ
     * @return список пар
     */
    private fun getLeftSideRightChain(s: Symbol): List<SimpleEntry<NonTerminal, Array<Symbol>>> {
        val list = ArrayList<SimpleEntry<NonTerminal, Array<Symbol>>>()
        for (r in rules) {
            var rightChain = r.rightSide
            val index = Arrays.asList(*rightChain).indexOf(s)
            if (index != -1) {
                rightChain = Arrays.copyOfRange(rightChain, index + 1, rightChain.size)
                list.add(SimpleEntry<NonTerminal, Array<Symbol>>(r.leftSide as NonTerminal?, rightChain))
            }
        }
        return list
    }

    /**
     * Возвращает правило с указанной левой и правой стороной
     *
     * @param leftSide символ в левой части производства
     * @param rightSide символы в правой части
     * @return правило с указанием левой и правой стороны
     * или `null`, если таковое правило не существует в грамматике
     */
    private fun getRule(leftSide: NonTerminal, rightSide: Array<Symbol>): Rule? {
        val setOfRules = getRulesWithLeftSide(leftSide)
        for (r in setOfRules) {
            if (rightSide.size != r.rightSide.size)
                continue
            for (i in rightSide.indices) {
                if (r.rightSide[i] !== rightSide[i])
                    break
                else {
                    if (i == rightSide.size - 1) {
                        return r
                    }
                }
            }
        }
        return null
    }
}