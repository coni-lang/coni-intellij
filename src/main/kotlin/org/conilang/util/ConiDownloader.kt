package org.conilang.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.util.io.HttpRequests
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.util.zip.ZipInputStream

object ConiDownloader {

    fun downloadBinary(project: Project?) {
        val arch = System.getProperty("os.arch").lowercase()
        val archName = if (arch.contains("aarch64") || arch.contains("arm")) "arm64" else "x64"

        val platform = when {
            ConiExecutable.isWindows() -> "win32"
            ConiExecutable.isMac() -> "darwin"
            else -> "linux"
        }

        val suffix = if (ConiExecutable.isWindows()) ".zip" else ".tar.gz"
        val downloadUrl = "https://coni-lang.org/downloads/coni-$platform-$archName$suffix"

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Downloading Coni Binary", false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                indicator.text = "Downloading Coni language server..."

                try {
                    val globalDir = ConiExecutable.getGlobalConiDir()
                    val archiveFile = File(globalDir, "downloaded$suffix")

                    HttpRequests.request(downloadUrl).connect { request ->
                        val connection = request.connection
                        val contentLength = connection.contentLength.toLong()
                        request.saveToFile(archiveFile, indicator)
                    }

                    indicator.text = "Extracting..."
                    indicator.isIndeterminate = true

                    // Extract archive
                    if (ConiExecutable.isWindows()) {
                        extractZip(archiveFile, globalDir)
                    } else {
                        // Use tar command for macOS/Linux to preserve permissions and easily handle tar.gz
                        val process = ProcessBuilder("tar", "-xzf", archiveFile.absolutePath, "-C", globalDir.absolutePath)
                            .start()
                        process.waitFor()
                    }

                    archiveFile.delete()

                    // Ensure extracted file exists and rename if needed
                    val expectedExtractedName = "coni-$platform-$archName" + (if (ConiExecutable.isWindows()) ".exe" else "")
                    val extractedFile = File(globalDir, expectedExtractedName)
                    val destinationFile = ConiExecutable.getGlobalConiPath()

                    if (extractedFile.exists()) {
                        if (destinationFile.exists()) destinationFile.delete()
                        extractedFile.renameTo(destinationFile)
                    } else if (!destinationFile.exists()) {
                        // Fallback check if it extracted as just 'coni'
                        val defaultExtract = File(globalDir, ConiExecutable.getConiFileName())
                        if (defaultExtract.exists() && defaultExtract != destinationFile) {
                            defaultExtract.renameTo(destinationFile)
                        }
                    }

                    if (!ConiExecutable.isWindows()) {
                        destinationFile.setExecutable(true, false)
                    }

                    // Download libmlx_c.dylib for macOS
                    if (ConiExecutable.isMac()) {
                        downloadMlxLib(indicator, globalDir)
                    }

                    ApplicationManager.getApplication().invokeLater {
                        Messages.showInfoMessage("Coni binary downloaded successfully to ${destinationFile.absolutePath}.", "Download Complete")
                    }

                } catch (e: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog("Failed to download Coni binary: ${e.message}", "Download Failed")
                    }
                }
            }
        })
    }

    private fun extractZip(zipFile: File, targetDir: File) {
        ZipInputStream(zipFile.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val newFile = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    newFile.outputStream().use { fos ->
                        zis.copyTo(fos)
                    }
                }
                entry = zis.nextEntry
            }
        }
    }

    private fun downloadMlxLib(indicator: ProgressIndicator, globalDir: File) {
        indicator.text = "Downloading libmlx_c.dylib..."
        indicator.isIndeterminate = true
        try {
            val evalDir = File(globalDir, "evaluator")
            if (!evalDir.exists()) evalDir.mkdirs()
            val dylibFile = File(evalDir, "libmlx_c.dylib")
            HttpRequests.request("https://coni-lang.org/downloads/libmlx_c.dylib").saveToFile(dylibFile, indicator)
        } catch (e: Exception) {
            // Ignore mlx download failure as it's optional for some cases
        }
    }
}
