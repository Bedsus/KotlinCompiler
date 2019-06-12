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
    private var level = 0
    /**
     * Основной узел дерева
     */
    private val treeNode = TreeNode<T>(level, "main", mutableMapOf())
    /**
     * Текуший узел. В него будут добавлятся переменные
     */
    private var currentTree = treeNode

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
        currentTree = currentTree.parent ?: return
        level--
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
                throw AnalyzerException("Таблица символов: Попытка получения данных у несуществующей переменной ${token.position()}")
            }
        }
        return variable.value
    }

    fun showTreeNode() = currentTree.toString()
}


/**
 * Дерево времени жизни у переменных
 *
 * @param number имя переменной
 * @param variables тип переменной
 */
class TreeNode<T>(private val number: Int, private val name: String, val variables : MutableMap<String, Variable<T>>) {

    var parent : TreeNode<T>? = null
    private var children = mutableListOf<TreeNode<T>>()

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
