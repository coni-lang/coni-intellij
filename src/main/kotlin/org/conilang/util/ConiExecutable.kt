package org.conilang.util

import com.intellij.openapi.project.Project
import java.io.File

object ConiExecutable {

    private val OS = System.getProperty("os.name").lowercase()
    
    /**
     * Resolves the path to the 'coni' executable.
     * Checks in order:
     * 1. ~/.coni/bin/coni (or .exe on Windows)
     * 2. Local project fallback (if provided)
     * 3. Default "coni" (expecting it to be in PATH)
     */
    fun resolve(project: Project? = null): String {
        val globalConi = getGlobalConiPath()
        if (globalConi.exists() && globalConi.canExecute()) {
            return globalConi.absolutePath
        }

        if (project != null) {
            val projectBasePath = project.basePath
            if (projectBasePath != null) {
                val localConi = File(projectBasePath, getConiFileName())
                if (localConi.exists() && localConi.canExecute()) {
                    return localConi.absolutePath
                }
            }
        }

        return "coni"
    }

    fun isDownloaded(): Boolean {
        return getGlobalConiPath().let { it.exists() && it.canExecute() }
    }

    fun isMac(): Boolean = OS.contains("mac")
    fun isWindows(): Boolean = OS.contains("win")
    fun isLinux(): Boolean = OS.contains("nix") || OS.contains("nux") || OS.contains("aix")

    fun getConiFileName(): String {
        return if (isWindows()) "coni.exe" else "coni"
    }

    fun getGlobalConiDir(): File {
        val userHome = System.getProperty("user.home")
        val dir = File(userHome, ".coni/bin")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getGlobalConiPath(): File {
        return File(getGlobalConiDir(), getConiFileName())
    }
}
