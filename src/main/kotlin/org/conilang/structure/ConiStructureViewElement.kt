package org.conilang.structure

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import org.conilang.psi.ConiFile
import org.conilang.psi.ConiTypes

class ConiStructureViewElement(private val myElement: PsiElement) : StructureViewTreeElement {
    override fun getValue(): Any = myElement

    override fun navigate(requestFocus: Boolean) {
        if (myElement is com.intellij.pom.Navigatable) {
            myElement.navigate(requestFocus)
        }
    }

    override fun canNavigate(): Boolean = myElement is com.intellij.pom.Navigatable && myElement.canNavigate()
    override fun canNavigateToSource(): Boolean = myElement is com.intellij.pom.Navigatable && myElement.canNavigateToSource()

    override fun getPresentation(): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText(): String? {
                return if (myElement is ConiFile) myElement.name else myElement.text
            }
            override fun getLocationString(): String? = null
            override fun getIcon(unused: Boolean) = null
        }
    }

    override fun getChildren(): Array<TreeElement> {
        if (myElement is ConiFile) {
            val children = mutableListOf<TreeElement>()
            var i = 0
            val siblings = myElement.children
            while (i < siblings.size) {
                if (siblings[i].node.elementType == ConiTypes.LPAREN) {
                    var j = i + 1
                    while (j < siblings.size && siblings[j].node.elementType == TokenType.WHITE_SPACE) j++
                    if (j < siblings.size) {
                        val keywordNode = siblings[j]
                        if (keywordNode.node.elementType == ConiTypes.KEYWORD) {
                            val kwText = keywordNode.text
                            if (kwText.startsWith("def")) {
                                var k = j + 1
                                while (k < siblings.size && siblings[k].node.elementType == TokenType.WHITE_SPACE) k++
                                if (k < siblings.size) {
                                    val idNode = siblings[k]
                                    if (idNode.node.elementType == ConiTypes.IDENTIFIER) {
                                        children.add(ConiDefElement(idNode, kwText))
                                    }
                                }
                            }
                        }
                    }
                }
                i++
            }
            return children.toTypedArray()
        }
        return emptyArray()
    }
}
