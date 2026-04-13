package org.conilang.psi

import com.intellij.psi.tree.IElementType
import org.conilang.ConiLanguage

class ConiTokenType(debugName: String) : IElementType(debugName, ConiLanguage.INSTANCE) {
    override fun toString(): String = "ConiTokenType." + super.toString()
}
