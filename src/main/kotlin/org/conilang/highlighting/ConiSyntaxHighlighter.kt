package org.conilang.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import org.conilang.lexer.ConiLexerAdapter
import org.conilang.psi.ConiTypes

class ConiSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        val KEYWORD = createTextAttributesKey("CONI_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val STRING = createTextAttributesKey("CONI_STRING", DefaultLanguageHighlighterColors.STRING)
        val NUMBER = createTextAttributesKey("CONI_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val COMMENT = createTextAttributesKey("CONI_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val IDENTIFIER = createTextAttributesKey("CONI_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
        val KEYWORD_SYM = createTextAttributesKey("CONI_KEYWORD_SYM", DefaultLanguageHighlighterColors.METADATA)
        val BRACKETS = createTextAttributesKey("CONI_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
        val PARENTHESES = createTextAttributesKey("CONI_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
        val BRACES = createTextAttributesKey("CONI_BRACES", DefaultLanguageHighlighterColors.BRACES)
        val BAD_CHARACTER = createTextAttributesKey("CONI_BAD_CHARACTER", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)

        private val BAD_CHAR_KEYS = arrayOf(BAD_CHARACTER)
        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val STRING_KEYS = arrayOf(STRING)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val COMMENT_KEYS = arrayOf(COMMENT)
        private val IDENTIFIER_KEYS = arrayOf(IDENTIFIER)
        private val KEYWORD_SYM_KEYS = arrayOf(KEYWORD_SYM)
        private val BRACKET_KEYS = arrayOf(BRACKETS)
        private val PARENTHESIS_KEYS = arrayOf(PARENTHESES)
        private val BRACE_KEYS = arrayOf(BRACES)
        private val EMPTY_KEYS = arrayOf<TextAttributesKey>()
    }

    override fun getHighlightingLexer(): Lexer = ConiLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            ConiTypes.KEYWORD -> KEYWORD_KEYS
            ConiTypes.STRING -> STRING_KEYS
            ConiTypes.NUMBER -> NUMBER_KEYS
            ConiTypes.COMMENT -> COMMENT_KEYS
            ConiTypes.IDENTIFIER -> IDENTIFIER_KEYS
            ConiTypes.KEYWORD_SYM -> KEYWORD_SYM_KEYS
            ConiTypes.LPAREN, ConiTypes.RPAREN -> PARENTHESIS_KEYS
            ConiTypes.LBRACKET, ConiTypes.RBRACKET -> BRACKET_KEYS
            ConiTypes.LBRACE, ConiTypes.RBRACE -> BRACE_KEYS
            com.intellij.psi.TokenType.BAD_CHARACTER -> BAD_CHAR_KEYS
            else -> EMPTY_KEYS
        }
    }
}
