package org.conilang

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class ConiFileType private constructor() : LanguageFileType(ConiLanguage.INSTANCE) {
    override fun getName(): String = "Coni File"
    override fun getDescription(): String = "Coni language file"
    override fun getDefaultExtension(): String = "coni"
    override fun getIcon(): Icon = ConiIcons.FILE

    companion object {
        val INSTANCE = ConiFileType()
    }
}
