package org.conilang

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.conilang.lexer.ConiLexerAdapter
import org.conilang.psi.ConiFile
import org.conilang.psi.ConiTypes

class ConiParserDefinition : ParserDefinition {
    companion object {
        val FILE = IFileElementType(ConiLanguage.INSTANCE)
    }

    override fun createLexer(project: Project): Lexer = ConiLexerAdapter()

    override fun createParser(project: Project): PsiParser {
        return PsiParser { root, builder ->
            val rootMarker = builder.mark()
            while (!builder.eof()) {
                builder.advanceLexer()
            }
            rootMarker.done(root)
            builder.treeBuilt
        }
    }

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = TokenSet.create(ConiTypes.COMMENT)

    override fun getStringLiteralElements(): TokenSet = TokenSet.create(ConiTypes.STRING)

    override fun createElement(node: ASTNode): PsiElement {
        return com.intellij.extapi.psi.ASTWrapperPsiElement(node)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return ConiFile(viewProvider)
    }
}
