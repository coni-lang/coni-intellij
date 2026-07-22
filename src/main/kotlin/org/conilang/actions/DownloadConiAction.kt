package org.conilang.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.conilang.util.ConiDownloader

class DownloadConiAction : AnAction("Download Coni Toolchain", "Downloads and sets up the Coni language server", null) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        ConiDownloader.downloadBinary(project)
    }
}
