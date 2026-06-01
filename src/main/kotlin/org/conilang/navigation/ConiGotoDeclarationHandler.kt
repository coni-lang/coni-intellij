package org.conilang.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import java.io.File

class ConiGotoDeclarationHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor?): Array<PsiElement>? {
        if (sourceElement == null || editor == null) return null
        val project = sourceElement.project
        val containingFile = sourceElement.containingFile ?: return null
        
        // Only target .coni files
        if (!containingFile.name.endsWith(".coni")) return null
        
        val text = containingFile.text ?: return null
        val word = getWordAt(text, offset) ?: return null
        
        val targets = resolveWordToTargets(word, containingFile, project)
        if (targets.isEmpty()) return null
        return targets.toTypedArray()
    }
    
    private fun getWordAt(text: String, offset: Int): String? {
        if (offset < 0 || offset >= text.length) return null
        
        // Find start of the word
        var start = offset
        while (start > 0 && isWordChar(text[start - 1])) {
            start--
        }
        
        // Find end of the word
        var end = offset
        while (end < text.length && isWordChar(text[end])) {
            end++
        }
        
        if (start >= end) return null
        return text.substring(start, end)
    }
    
    private fun isWordChar(c: Char): Boolean {
        return c.isLetterOrDigit() || c == '-' || c == '_' || c == '*' || c == '+' || c == '/' || c == '?' || c == '!' || c == '<' || c == '>' || c == '=' || c == '.' || c == ':' || c == '"'
    }
    
    private fun resolveWordToTargets(word: String, containingFile: PsiFile, project: Project): List<PsiElement> {
        val psiManager = PsiManager.getInstance(project)

        // 0. Clicked on a string path
        if (word.startsWith("\"") && word.endsWith("\"")) {
            val rawPath = word.substring(1, word.length - 1)
            val file = findFileByRelativePath(rawPath, containingFile, project)
            if (file != null) {
                return listOf(file)
            }
            return emptyList()
        }
        
        // 1. Check local document first
        val localDef = findDefinitionInFile(word, containingFile)
        if (localDef != null) {
            return listOf(localDef)
        }
        
        // 2. Handle namespaced symbols: alias/fnName
        if (word.contains("/")) {
            val parts = word.split("/")
            if (parts.size == 2) {
                val alias = parts[0]
                val fnName = parts[1]
                
                val requireRegex = """\(require\s+"([^"]+)"\s+:as\s+${Regex.escape(alias)}\)""".toRegex()
                val match = requireRegex.find(containingFile.text)
                if (match != null) {
                    val importPath = match.groupValues[1]
                    val targetFile = findFileByRelativePath(importPath, containingFile, project)
                    if (targetFile != null) {
                        val def = findDefinitionInFile(fnName, targetFile)
                        if (def != null) {
                            return listOf(def)
                        }
                    }
                }
            }
        }
        
        // 3. Handle bare symbols
        // 3a. Search all :all requires
        val requireAllRegex = """\(require\s+"([^"]+)"\s+:all\s*\)""".toRegex()
        val matches = requireAllRegex.findAll(containingFile.text)
        for (m in matches) {
            val importPath = m.groupValues[1]
            val targetFile = findFileByRelativePath(importPath, containingFile, project)
            if (targetFile != null) {
                val def = findDefinitionInFile(word, targetFile)
                if (def != null) {
                    return listOf(def)
                }
            }
        }
        
        // 3b. Search core.coni specifically
        val coreVFiles = FilenameIndex.getVirtualFilesByName(project, "core.coni", true, GlobalSearchScope.allScope(project))
        for (virtualFile in coreVFiles) {
            val coreFile = psiManager.findFile(virtualFile) ?: continue
            val def = findDefinitionInFile(word, coreFile)
            if (def != null) {
                return listOf(def)
            }
        }
        
        // 3c. Fallback: Project-wide search in all .coni files
        val projectFiles = FilenameIndex.getAllFilesByExt(project, "coni", GlobalSearchScope.projectScope(project))
        for (virtualFile in projectFiles) {
            val psiFile = psiManager.findFile(virtualFile) ?: continue
            if (psiFile != containingFile) {
                val def = findDefinitionInFile(word, psiFile)
                if (def != null) {
                    return listOf(def)
                }
            }
        }
        
        return emptyList()
    }
    
    private fun findFileByRelativePath(relPath: String, currentFile: PsiFile, project: Project): PsiFile? {
        val projectBaseDir = project.basePath
        val psiManager = PsiManager.getInstance(project)
        
        // Try directly from project root
        if (projectBaseDir != null) {
            val f = File(projectBaseDir, relPath)
            if (f.exists()) {
                val vf = com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByIoFile(f)
                if (vf != null) {
                    val pf = psiManager.findFile(vf)
                    if (pf != null) return pf
                }
            }
            
            // Try inside coni-lang-gitea subdirectory
            val f2 = File(File(projectBaseDir, "coni-lang-gitea"), relPath)
            if (f2.exists()) {
                val vf2 = com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByIoFile(f2)
                if (vf2 != null) {
                    val pf2 = psiManager.findFile(vf2)
                    if (pf2 != null) return pf2
                }
            }
        }
        
        // Try relative to the current file's parent folder
        val parentDir = currentFile.virtualFile?.parent
        if (parentDir != null) {
            val vf = parentDir.findFileByRelativePath(relPath)
            if (vf != null) {
                val pf = psiManager.findFile(vf)
                if (pf != null) return pf
            }
        }
        
        return null
    }
    
    private fun findDefinitionInFile(name: String, file: PsiFile): PsiElement? {
        val text = file.text ?: return null
        val escapedName = Regex.escape(name)
        val defRegex = """\(\s*(?:def|defn|defmacro|defn-|defmacro-)\s+$escapedName(?:\s|$)""".toRegex()
        val match = defRegex.find(text)
        if (match != null) {
            val offset = match.range.start
            return file.findElementAt(offset) ?: file
        }
        return null
    }
}
