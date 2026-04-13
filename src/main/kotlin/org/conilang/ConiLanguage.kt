package org.conilang

import com.intellij.lang.Language

class ConiLanguage private constructor() : Language("Coni") {
    companion object {
        val INSTANCE = ConiLanguage()
    }
}
