package org.conilang.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import java.net.Socket
import java.io.OutputStreamWriter
import java.io.InputStreamReader

class EvaluateSelectionAction : AnAction("Evaluate Selection in REPL") {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        val selectionModel = editor.selectionModel
        
        val code = if (selectionModel.hasSelection()) {
            selectionModel.selectedText
        } else {
            val caretModel = editor.caretModel
            val line = caretModel.logicalPosition.line
            val startOffset = editor.document.getLineStartOffset(line)
            val endOffset = editor.document.getLineEndOffset(line)
            editor.document.getText(com.intellij.openapi.util.TextRange(startOffset, endOffset))
        }

        if (code.isNullOrBlank()) return

        Thread {
            try {
                val socket = Socket("127.0.0.1", 3333)
                val writer = OutputStreamWriter(socket.getOutputStream())
                val reader = InputStreamReader(socket.getInputStream())

                writer.write("$code\nexit\n")
                writer.flush()

                var output = reader.readText()
                socket.close()

                val lines = output.split("\n")
                val cleanLines = mutableListOf<String>()
                var started = false
                for (line in lines) {
                    var clean = line.trimEnd().replace(Regex("\\x1b\\[[0-9;]*m"), "")
                    while (clean.matches(Regex("^(coni>|\\.\\.\\.)\\s*.*"))) {
                        clean = clean.replace(Regex("^(coni>|\\.\\.\\.)\\s*"), "")
                    }
                    clean = clean.replace(Regex("Bye!$"), "")
                    
                    if (started) {
                        if (clean.isNotEmpty() && clean != "exit") {
                            cleanLines.add(clean)
                        }
                    } else {
                        if (clean.contains("Type 'exit' to disconnect.")) {
                            started = true
                        }
                    }
                }
                
                val finalOutput = cleanLines.joinToString("\n").trim()
                if (finalOutput.isNotEmpty()) {
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                        WriteCommandAction.runWriteCommandAction(project) {
                            val insertPos = selectionModel.selectionEnd
                            val formatted = if (finalOutput.contains("\n")) {
                                "\n" + finalOutput.split("\n").joinToString("\n") { ";; $it" }
                            } else {
                                " ;; => $finalOutput"
                            }
                            editor.document.insertString(insertPos, formatted)
                        }
                        // Optionally show a balloon or console message here instead of inserting
                    }
                }
            } catch (ex: Exception) {
                com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(project, "Failed to connect to Coni REPL on 127.0.0.1:3333.\nIs it running? (Error: ${ex.message})", "REPL Connection Failed")
                }
            }
        }.start()
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = editor != null
    }
}
