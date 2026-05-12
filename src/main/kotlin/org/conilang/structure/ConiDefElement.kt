package org.conilang.structure

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import javax.swing.Icon
import com.intellij.icons.AllIcons

class ConiDefElement(
    private val myElement: PsiElement,
    private val kind: String,
    private val myChildren: MutableList<TreeElement> = mutableListOf()
) : StructureViewTreeElement {
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
            override fun getPresentableText(): String? = myElement.text
            override fun getLocationString(): String? = null
            override fun getIcon(unused: Boolean): Icon? {
                return if (kind.startsWith("defn") || kind.startsWith("defmacro") || kind == "method") {
                    AllIcons.Nodes.Function
                } else if (kind == "defrecord") {
                    AllIcons.Nodes.Class
                } else if (kind == "defprotocol") {
                    AllIcons.Nodes.Interface
                } else {
                    AllIcons.Nodes.Variable
                }
            }
        }
    }

    fun addChild(child: TreeElement) {
        myChildren.add(child)
    }

    override fun getChildren(): Array<TreeElement> = myChildren.toTypedArray()
}
