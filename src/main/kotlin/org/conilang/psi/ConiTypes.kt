package org.conilang.psi

import com.intellij.psi.tree.IElementType

object ConiTypes {
    @JvmField val COMMENT: IElementType = ConiTokenType("COMMENT")
    @JvmField val STRING: IElementType = ConiTokenType("STRING")
    @JvmField val NUMBER: IElementType = ConiTokenType("NUMBER")
    @JvmField val KEYWORD: IElementType = ConiTokenType("KEYWORD")
    @JvmField val KEYWORD_SYM: IElementType = ConiTokenType("KEYWORD_SYM")
    @JvmField val IDENTIFIER: IElementType = ConiTokenType("IDENTIFIER")
    @JvmField val LPAREN: IElementType = ConiTokenType("LPAREN")
    @JvmField val RPAREN: IElementType = ConiTokenType("RPAREN")
    @JvmField val LBRACKET: IElementType = ConiTokenType("LBRACKET")
    @JvmField val RBRACKET: IElementType = ConiTokenType("RBRACKET")
    @JvmField val LBRACE: IElementType = ConiTokenType("LBRACE")
    @JvmField val RBRACE: IElementType = ConiTokenType("RBRACE")
    @JvmField val HASH: IElementType = ConiTokenType("HASH")
}
