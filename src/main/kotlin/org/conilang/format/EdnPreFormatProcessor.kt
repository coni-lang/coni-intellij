package org.conilang.format

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.intellij.psi.PsiDocumentManager
import com.intellij.openapi.command.WriteCommandAction

class EdnPreFormatProcessor : PreFormatProcessor {
    override fun process(element: ASTNode, range: TextRange): TextRange {
        val psi = element.psi ?: return range
        val file = psi.containingFile ?: return range
        
        // We only format if it's an edn file and we are at the file level
        if (file.name.endsWith(".edn") && psi == file) {
            val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return range
            val text = document.text
            try {
                val formatted = EdnFormatter.format(text)
                if (formatted != text) {
                    WriteCommandAction.runWriteCommandAction(file.project) {
                        document.setText(formatted)
                    }
                    return TextRange(0, formatted.length)
                }
            } catch (e: Exception) {
                // Fail gracefully, don't crash the formatter
                e.printStackTrace()
            }
        }
        return range
    }
}
