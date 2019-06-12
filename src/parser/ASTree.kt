package parser

class ASTBuilder {

    private val asTree = ASTree("main")
    private var currentTree = asTree


    fun createNode(name: String) {
        val newTree = ASTree(name)
        currentTree.addChild(newTree)
        currentTree = newTree
    }

    fun previousNode() {
        currentTree = currentTree.parent ?: return
    }

    override fun toString() = currentTree.toString()

}

class ASTree(val name: String) {

    var parent: ASTree? = null
    val children  = mutableListOf<ASTree>()

    fun addChild(node : ASTree) {
        children.add(node)
        node.parent = this
    }

    override fun toString(): String {
        var s = "$name\n"
        if (children.isNotEmpty()) {
            for (node in children) {
                s += node.toString()
            }
        }
        return s
    }

}

