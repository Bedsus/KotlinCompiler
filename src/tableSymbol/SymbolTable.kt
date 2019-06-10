package tableSymbol


class SymbolTable {

    /**  Уровень вложенности текущей области видимости  */
    private var curLevel = 0
    /**  Узел объекта для ошибочных символов  */
    private var undefVar = Variable("undef", 0)
    /**  Верхняя область процедуры */
    private var topScope: Scope? = null

    fun printTable() {
        println("Table contents: ")
        var scope = topScope
        while (scope != null) {
            println(scope)
            scope.printLocals()
            scope = scope.next
        }
    }

    /**
     * Открыть новую область и сделать ее текущей областью
     */
    fun openScope(type: Int) {
        val new_scope = Scope(type, this.curLevel++)
        new_scope.next = topScope
        topScope = new_scope
    }

    /**
     * Закрыть текущую область
     */
    fun closeScope() {
        topScope = topScope!!.next
        curLevel--
    }

    // TODO: should the store location come from the parser?
    fun addFunction(name: String, type: Int, label: Int): Function {
        val new_function = Function(name, type, label)
        topScope!!.addSymbol(new_function)
        return new_function
    }

    fun addVariable(name: String, type: Int): Variable {
        val new_variable = Variable(name, type)
        topScope!!.addSymbol(new_variable)
        return new_variable
    }

    /**
     * Искать имя во всех открытых областях и возвращать его объектный узел
     */
    fun findSymbol(name: String): Symbol_ {
        var scope = this.topScope
        var symbol: Symbol_?
        while (scope != null) {
            symbol = scope.findSymbol(name)
            if (symbol != null) {
                return symbol
            }
            scope = scope.next
        }

       // parser.SemErr("'$name' is undeclared")
        return undefVar
    }

    /**
     * Время жизны
     */
    inner class Scope(var returnType: Int, var level: Int) {
        var next: Scope? = null
        var frameSize = 0

        /**
         * Локально объявленные объекты
         */
        private val locals = mutableMapOf<String, Symbol_>()

        fun printLocals() {
            for ((name, value) in locals) {
                val variable = value as Variable
                for (i in 0 until variable.level) {
                    print("  ")
                }
                println(name + ": " + Integer.toString(variable.adr))
            }
        }

        private fun checkUniqueSymbolName(name: String) {
            for (localName in locals.keys) {
                if (localName == name) {
                  //  parser.SemErr("name '$name' declared twice")
                }
            }
        }

        fun addSymbol(fn: Function) {
            checkUniqueSymbolName(fn.name)
            locals[fn.name] = fn
        }

        fun addSymbol(variable: Variable) {
            checkUniqueSymbolName(variable.name)
            locals[variable.name] = variable
            variable.adr = ++frameSize
        }

        fun findSymbol(name: String): Symbol_? {
            for ((symbolName, symbolValue) in locals) {
                if (name == symbolName) {
                    return symbolValue
                }
            }
            return null
        }
    }

    abstract inner class Symbol_ {
        abstract val name: String
        abstract val type: Int // For functions this is the return type
    }

    inner class Function(override val name: String, override val type: Int, var label: Int) : Symbol_()  {
        private var parameters = mutableListOf<Variable>()

        fun addParameter(variable: Variable) {
            parameters.add(variable)
        }
    }

    inner class Variable(override val name: String, override val type: Int) : Symbol_() {
        var adr: Int = 0
        var level: Int = 0
    }
}