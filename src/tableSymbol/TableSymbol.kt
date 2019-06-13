package tableSymbol

import AnalyzerException
import blueColor
import defaultColor
import lexer.Token
import lexer.TokenType
import violetColor

/**
 * Реализация таблицы символов.
 * Переменые распредиляются по времени жизни в отдельных списках.
 * Каждый список зависит от другово, организуются ветки зависимостей (дерево)
 */
class TableSymbol<T> {

    /**
     * Уровень погружения узла дерева, увеличивается каждый раз, как создается узел
     */
    private var level : Int = 0
    /**
     * Основной узел дерева
     */
    private val treeNode = TreeNode<T>(level, "MAIN", mutableMapOf())
    /**
     * Текуший узел. В него будут добавлятся переменные
     */
    var currentTree = treeNode

    /**
     * Создание узла (нового scope)
     */
    fun createNode(name: String) {
        level++
        val newTree = TreeNode<T>(level, name, mutableMapOf())
        currentTree.addChild(newTree)
        currentTree = newTree
    }

    /**
     * Откат по дереву на предыдущий узел (уровень)
     */
    fun previousTreeNode() {
        previous()
        level--
        if (level == 2) {
            previous()
            level--
        }
    }

    fun previous() {
        currentTree = currentTree.parent ?: return
    }

    fun setCurrentNode(name: String) : Boolean {
        val nodes: MutableList<TreeNode<T>>? = currentTree.children
        for(node in nodes ?: mutableListOf()) {
            if(node.name == name){
                currentTree = node
                return true
            }
        }
        return false
      //  throw AnalyzerException("Таблица символов: Узел $name среди ${currentTree.children} не найден")
    }

    fun showCurrentValue() : String {
        var s = ""
        for ((key, value) in currentTree.variables) {
            s += " $blueColor  ${value.properties} $key : ${value.type} $defaultColor \n"
        }
        return s
    }

    /**
     * Добавление переменной
     */
    fun addVariable(name: String, properties: Properties, type: TokenType, value: T, token: Token) {
        val currentVariable = currentTree.variables[name]
        if (currentVariable != null) {
            throw AnalyzerException("Таблица символов: Попытка добавления существующей переменной ${token.position()}")
        }
        currentTree.variables[name] = Variable(properties, type, value)
    }

    /**
     * Получить данные переменной
     */
    fun getValue(name: String, token: Token) : T {
        var variable : Variable<T>? = currentTree.variables[name]
        var treeNode: TreeNode<T>? = currentTree
        while (variable == null) {
            if (treeNode != null) {
                variable = treeNode.variables[name]
                treeNode = treeNode.parent
            } else {
                throw AnalyzerException("Таблица символов: Попытка получения данных у несуществующей переменной ${token.tokenString} ${token.position()}")
            }
        }
        return variable.value
    }

    fun showTreeNode() : String = currentTree.toString()
}


/**
 * Дерево времени жизни у переменных
 *
 * @param number имя переменной
 * @param variables тип переменной
 */
class TreeNode<T>(val number: Int, val name: String, val variables : MutableMap<String, Variable<T>>) {

    var parent : TreeNode<T>? = null
    var children = mutableListOf<TreeNode<T>>()

    fun addChild(node : TreeNode<T>) {
        children.add(node)
        node.parent = this
    }

    override fun toString(): String {
        var s = ""
        if (variables.values.isNotEmpty()) {
            s += "$number: $violetColor $name $defaultColor [parent: $violetColor ${parent?.name
                ?: "-"}$defaultColor]\n"
            for ((key, value) in variables) {
                s += " $blueColor  ${value.properties} $key : ${value.type} $defaultColor \n"
            }
        }
        if (children.isNotEmpty()) {
            for (node in children) {
                s += node.toString()
            }
        }
        return s
    }
}

/**
 * Информация о переменной
 *
 * @param properties свойства переменной
 * @param type тип переменной
 * @param value вложенное значение переменной
 */
class Variable<T>(val properties: Properties, val type: TokenType, var value: T)

/**
 * Свойства переменой
 * var - изменяемые (mutable)
 * val - неизменяемые (read-only)
 */
enum class Properties { Var, Val; }
