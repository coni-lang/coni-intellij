package org.conilang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import org.conilang.ConiFileType
import org.conilang.ConiLanguage

class ConiFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ConiLanguage.INSTANCE) {
    override fun getFileType(): FileType = ConiFileType.INSTANCE
    override fun toString(): String = "Coni File"
}
