package tableSymbol

/**
 * Реализация таблицы символов.
 * Переменые распредиляются по времени жизни в отдельных списках.
 * Каждый список зависит от другово, организуются ветки зависимостей (дерево)
 */
class TableSymbol<T> {

    /**
     * Номер узла дерева, увеличивается каждый раз, как создается узел
     */
    private var nodeNumber = 0
    /**
     * Основной узел дерева
     */
    private val treeNode = TreeNode<T>( nodeNumber, mutableMapOf())
    /**
     * Текуший узел. В него будут добавлятся переменные
     */
    private var currentTree : TreeNode<T> = treeNode

    /**
     * Создание узла (нового scope)
     */
    fun createNode() {
        nodeNumber++
        val newTree = TreeNode<T>( nodeNumber, mutableMapOf())
        currentTree.addChild(newTree)
        currentTree = newTree
    }

    /**
     * Откат по дереву на предыдущий узел
     */
    fun previousTreeNode() {
        currentTree = currentTree.parent ?: return
           // throw IllegalArgumentException("Попытка отката к несуществующему родительскому узлу")
    }

    /**
     * Добавление переменной
     */
    fun addVariable(name: String, properties: Properties, type: Type, value: T) {
        val currentVariable = currentTree.variables[name]
        if (currentVariable != null) {
            throw IllegalArgumentException("Попытка добавления существующей переменной")
        }
        currentTree.variables[name] = Variable(properties, type, value)
    }

    /**
     * Изменение значения у переменной
     */
    fun updateVariable(name: String, value: T) {
        var variable : Variable<T>? = currentTree.variables[name]
        var treeNode: TreeNode<T>? = currentTree
        while (variable == null) {
            if (treeNode != null) {
                variable = treeNode.variables[name]
                treeNode = treeNode.parent
            } else {
                throw IllegalArgumentException("Попытка изменения несуществующей переменной")
            }
        }
        variable.value = value
    }

    /**
     * Получить данные переменной
     */
    fun getValue(name: String) : T {
        val variable = currentTree.variables[name] ?:
            throw IllegalArgumentException("Попытка получения данных у несуществующей переменной")
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
class TreeNode<T>(private val number: Int, val variables : MutableMap<String, Variable<T>>) {

    var parent : TreeNode<T>? = null
    var children = mutableListOf<TreeNode<T>>()

    fun addChild(node : TreeNode<T>) {
        children.add(node)
        node.parent = this
    }

    override fun toString(): String {
        var s = "$number:\n"
        for((key, value) in variables){
            s += "${value.properties} $key : ${value.type} = ${value.value} \n"
        }
        if (children.isNotEmpty()) {
            s += " [" + children.map { it.toString() } + " ]"
        }
        return s
    }
}

/**
 * Информация о переменной
 *
 * @param properties свойства переменной
 * @param name имя переменной
 * @param type тип переменной
 * @param value вложенное значение переменной
 */
class Variable<T>(val properties: Properties, val type: Type, var value: T)

/**
 * Тип данных у переменной
 */
enum class Type { Int, Double; }

/**
 * Свойства переменой
 * var - изменяемые (mutable)
 * val - неизменяемые (read-only)
 */
enum class Properties { Var, Val; }