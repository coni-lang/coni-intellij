package org.conilang.format

enum class TokenType {
    WHITESPACE,
    COMMENT,
    BRACKET,
    TAG,
    STRING,
    CHAR,
    KEYWORD,
    BOOLEAN,
    NIL,
    NUMBER,
    SYMBOL
}

data class Token(val type: TokenType, val value: String)

sealed class Node {
    object NilNode : Node()
    data class LiteralNode(val value: String) : Node()
    data class CommentNode(val text: String) : Node()
    data class TaggedNode(val tag: String, val value: Node) : Node()
    data class ListNode(val elements: List<Node>) : Node()
    data class VectorNode(val elements: List<Node>) : Node()
    data class SetNode(val elements: List<Node>) : Node()
    data class MapEntryNode(val key: Node, val value: Node?) : Node()
    data class MapNode(val elements: List<Node>) : Node()
}

fun tokenize(src: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var i = 0
    while (i < src.length) {
        val char = src[i]
        
        // Whitespace and commas
        if (char.isWhitespace() || char == ',') {
            val start = i
            while (i < src.length && (src[i].isWhitespace() || src[i] == ',')) {
                i++
            }
            tokens.add(Token(TokenType.WHITESPACE, src.substring(start, i)))
            continue
        }
        
        // Comment
        if (char == ';') {
            val start = i
            while (i < src.length && src[i] != '\n' && src[i] != '\r') {
                i++
            }
            tokens.add(Token(TokenType.COMMENT, src.substring(start, i)))
            continue
        }
        
        // Set start or Tagged literal
        if (char == '#') {
            if (i + 1 < src.length && src[i + 1] == '{') {
                tokens.add(Token(TokenType.BRACKET, "#{"))
                i += 2
                continue
            }
            // Tagged value: e.g. #inst, #uuid, #custom/tag
            val start = i
            i++
            while (i < src.length && !src[i].isWhitespace() && src[i] != ',' &&
                   src[i] != '(' && src[i] != ')' && src[i] != '[' && src[i] != ']' &&
                   src[i] != '{' && src[i] != '}') {
                i++
            }
            tokens.add(Token(TokenType.TAG, src.substring(start, i)))
            continue
        }
        
        // Brackets
        if (char == '(' || char == ')' || char == '[' || char == ']' || char == '{' || char == '}') {
            tokens.add(Token(TokenType.BRACKET, char.toString()))
            i++
            continue
        }
        
        // String
        if (char == '"') {
            val start = i
            i++
            while (i < src.length) {
                if (src[i] == '\\') {
                    i += 2
                } else if (src[i] == '"') {
                    i++
                    break
                } else {
                    i++
                }
            }
            tokens.add(Token(TokenType.STRING, src.substring(start, minOf(i, src.length))))
            continue
        }
        
        // Character literal (e.g. \c, \newline, \space)
        if (char == '\\') {
            val start = i
            i++
            while (i < src.length && !src[i].isWhitespace() && src[i] != ',' &&
                   src[i] != '(' && src[i] != ')' && src[i] != '[' && src[i] != ']' &&
                   src[i] != '{' && src[i] != '}') {
                i++
            }
            tokens.add(Token(TokenType.CHAR, src.substring(start, i)))
            continue
        }
        
        // Symbol / Keyword / Number / Bool / Nil
        val start = i
        while (i < src.length && !src[i].isWhitespace() && src[i] != ',' &&
               src[i] != '(' && src[i] != ')' && src[i] != '[' && src[i] != ']' &&
               src[i] != '{' && src[i] != '}') {
            i++
        }
        val valStr = src.substring(start, i)
        if (valStr.isEmpty()) {
            i++ // safety fallback to prevent infinite loops
            continue
        }
        
        val type = when {
            valStr.startsWith(":") -> TokenType.KEYWORD
            valStr == "true" || valStr == "false" -> TokenType.BOOLEAN
            valStr == "nil" -> TokenType.NIL
            valStr[0].isDigit() || (valStr.length > 1 && (valStr[0] == '+' || valStr[0] == '-') && valStr[1].isDigit()) -> TokenType.NUMBER
            else -> TokenType.SYMBOL
        }
        tokens.add(Token(type, valStr))
    }
    return tokens
}

class EdnParser(private val tokens: List<Token>) {
    private var idx = 0
    
    private fun next(): Token? {
        while (idx < tokens.size && tokens[idx].type == TokenType.WHITESPACE) {
            idx++
        }
        if (idx >= tokens.size) return null
        return tokens[idx]
    }
    
    fun parse(): List<Node> {
        val root = mutableListOf<Node>()
        while (idx < tokens.size) {
            if (tokens[idx].type == TokenType.WHITESPACE) {
                idx++
                continue
            }
            val node = parseNode()
            if (node != null) {
                root.add(node)
            }
        }
        return root
    }
    
    private fun parseNode(): Node? {
        val t = next() ?: return null
        
        if (t.type == TokenType.COMMENT) {
            idx++
            return Node.CommentNode(t.value)
        }
        
        if (t.type == TokenType.TAG) {
            idx++
            val valueNode = parseNode() ?: Node.NilNode
            return Node.TaggedNode(t.value, valueNode)
        }
        
        if (t.type == TokenType.BRACKET) {
            when (t.value) {
                "(" -> {
                    idx++
                    val elements = mutableListOf<Node>()
                    while (true) {
                        val nextT = next()
                        if (nextT == null || (nextT.type == TokenType.BRACKET && nextT.value == ")")) {
                            if (nextT != null) idx++
                            break
                        }
                        val child = parseNode()
                        if (child != null) elements.add(child)
                    }
                    return Node.ListNode(elements)
                }
                "[" -> {
                    idx++
                    val elements = mutableListOf<Node>()
                    while (true) {
                        val nextT = next()
                        if (nextT == null || (nextT.type == TokenType.BRACKET && nextT.value == "]")) {
                            if (nextT != null) idx++
                            break
                        }
                        val child = parseNode()
                        if (child != null) elements.add(child)
                    }
                    return Node.VectorNode(elements)
                }
                "#{" -> {
                    idx++
                    val elements = mutableListOf<Node>()
                    while (true) {
                        val nextT = next()
                        if (nextT == null || (nextT.type == TokenType.BRACKET && nextT.value == "}")) {
                            if (nextT != null) idx++
                            break
                        }
                        val child = parseNode()
                        if (child != null) elements.add(child)
                    }
                    return Node.SetNode(elements)
                }
                "{" -> {
                    idx++
                    val elements = mutableListOf<Node>()
                    while (true) {
                        val nextT = next()
                        if (nextT == null || (nextT.type == TokenType.BRACKET && nextT.value == "}")) {
                            if (nextT != null) idx++
                            break
                        }
                        
                        if (nextT.type == TokenType.COMMENT) {
                            idx++
                            elements.add(Node.CommentNode(nextT.value))
                            continue
                        }
                        
                        // Parse key
                        val key = parseNode() ?: break
                        
                        // Parse value, skipping comment tokens but keeping them in elements
                        var value: Node? = null
                        while (true) {
                            val midT = next() ?: break
                            if (midT.type == TokenType.COMMENT) {
                                idx++
                                elements.add(Node.CommentNode(midT.value))
                            } else {
                                value = parseNode()
                                break
                            }
                        }
                        elements.add(Node.MapEntryNode(key, value))
                    }
                    return Node.MapNode(elements)
                }
                else -> {
                    // Unmatched bracket
                    idx++
                    return Node.LiteralNode(t.value)
                }
            }
        }
        
        idx++
        return Node.LiteralNode(t.value)
    }
}

object EdnFormatter {
    fun format(src: String): String {
        val tokens = tokenize(src)
        val parser = EdnParser(tokens)
        val nodes = parser.parse()
        return nodes.joinToString("\n") { formatNode(it, 0) }.trim() + "\n"
    }
    
    private fun isSimple(node: Node?): Boolean {
        if (node == null) return true
        return when (node) {
            is Node.NilNode -> true
            is Node.LiteralNode -> true
            is Node.CommentNode -> false
            is Node.TaggedNode -> isSimple(node.value)
            is Node.ListNode -> {
                if (node.elements.size > 8) false
                else node.elements.all { isSimple(it) } && estimateLength(node) < 50
            }
            is Node.VectorNode -> {
                if (node.elements.size > 8) false
                else node.elements.all { isSimple(it) } && estimateLength(node) < 50
            }
            is Node.SetNode -> {
                if (node.elements.size > 8) false
                else node.elements.all { isSimple(it) } && estimateLength(node) < 50
            }
            is Node.MapNode -> {
                if (node.elements.size > 4) false
                else {
                    node.elements.all {
                        when (it) {
                            is Node.CommentNode -> false
                            is Node.MapEntryNode -> isSimple(it.key) && isSimple(it.value)
                            else -> false
                        }
                    } && estimateLength(node) < 50
                }
            }
            is Node.MapEntryNode -> isSimple(node.key) && isSimple(node.value)
        }
    }
    
    private fun estimateLength(node: Node?): Int {
        if (node == null) return 0
        return when (node) {
            is Node.NilNode -> 3
            is Node.LiteralNode -> node.value.length
            is Node.CommentNode -> node.text.length + 1
            is Node.TaggedNode -> node.tag.length + 1 + estimateLength(node.value)
            is Node.ListNode -> {
                var len = 1 // '('
                node.elements.forEachIndexed { i, el ->
                    if (i > 0) len += 1
                    len += estimateLength(el)
                }
                len + 1 // ')'
            }
            is Node.VectorNode -> {
                var len = 1 // '['
                node.elements.forEachIndexed { i, el ->
                    if (i > 0) len += 1
                    len += estimateLength(el)
                }
                len + 1 // ']'
            }
            is Node.SetNode -> {
                var len = 2 // '#{'
                node.elements.forEachIndexed { i, el ->
                    if (i > 0) len += 1
                    len += estimateLength(el)
                }
                len + 1 // '}'
            }
            is Node.MapNode -> {
                var len = 1 // '{'
                node.elements.forEachIndexed { i, el ->
                    if (i > 0) len += 1
                    len += when (el) {
                        is Node.CommentNode -> el.text.length
                        is Node.MapEntryNode -> estimateLength(el.key) + 1 + estimateLength(el.value)
                        else -> 0
                    }
                }
                len + 1 // '}'
            }
            is Node.MapEntryNode -> estimateLength(node.key) + 1 + estimateLength(node.value)
        }
    }
    
    private fun formatNode(node: Node?, indentLevel: Int, indentStr: String = "  "): String {
        if (node == null) return ""
        val indent = indentStr.repeat(indentLevel)
        val nextIndent = indentStr.repeat(indentLevel + 1)
        
        return when (node) {
            is Node.NilNode -> "nil"
            is Node.LiteralNode -> node.value
            is Node.CommentNode -> node.text
            is Node.TaggedNode -> {
                val valStr = formatNode(node.value, indentLevel, indentStr)
                "${node.tag} $valStr"
            }
            is Node.ListNode -> {
                if (isSimple(node)) {
                    val inner = node.elements.joinToString(" ") { formatNode(it, 0, indentStr) }
                    "($inner)"
                } else {
                    val parts = node.elements.map { formatNode(it, indentLevel + 1, indentStr) }
                    val inner = parts.joinToString("\n") { "$nextIndent$it" }
                    "(\n$inner\n$indent)"
                }
            }
            is Node.VectorNode -> {
                if (isSimple(node)) {
                    val inner = node.elements.joinToString(" ") { formatNode(it, 0, indentStr) }
                    "[$inner]"
                } else {
                    val parts = node.elements.map { formatNode(it, indentLevel + 1, indentStr) }
                    val inner = parts.joinToString("\n") { "$nextIndent$it" }
                    "[\n$inner\n$indent]"
                }
            }
            is Node.SetNode -> {
                if (isSimple(node)) {
                    val inner = node.elements.joinToString(" ") { formatNode(it, 0, indentStr) }
                    "#{$inner}"
                } else {
                    val parts = node.elements.map { formatNode(it, indentLevel + 1, indentStr) }
                    val inner = parts.joinToString("\n") { "$nextIndent$it" }
                    "#{\n$inner\n$indent}"
                }
            }
            is Node.MapNode -> {
                if (isSimple(node)) {
                    val parts = mutableListOf<String>()
                    node.elements.forEach { el ->
                        if (el is Node.MapEntryNode) {
                            parts.add("${formatNode(el.key, 0, indentStr)} ${formatNode(el.value, 0, indentStr)}")
                        }
                    }
                    "{${parts.joinToString(" ")}}"
                } else {
                    val parts = mutableListOf<String>()
                    node.elements.forEach { el ->
                        when (el) {
                            is Node.CommentNode -> {
                                parts.add("$nextIndent${formatNode(el, indentLevel + 1, indentStr)}")
                            }
                            is Node.MapEntryNode -> {
                                val keyStr = formatNode(el.key, indentLevel + 1, indentStr)
                                val valStr = formatNode(el.value, indentLevel + 1, indentStr)
                                parts.add("$nextIndent$keyStr $valStr")
                            }
                            else -> {}
                        }
                    }
                    "{\n${parts.joinToString("\n")}\n$indent}"
                }
            }
            is Node.MapEntryNode -> {
                "${formatNode(node.key, indentLevel, indentStr)} ${formatNode(node.value, indentLevel, indentStr)}"
            }
        }
    }
}
