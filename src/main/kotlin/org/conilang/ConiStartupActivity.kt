package org.conilang

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.conilang.util.ConiExecutable
import org.conilang.util.ConiDownloader

class ConiStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        if (!ConiExecutable.isDownloaded() && ConiExecutable.resolve(project) == "coni") {
            // Check if "coni" is in PATH by running it with --version or similar
            val isInPath = try {
                val process = ProcessBuilder("coni", "--version").start()
                process.waitFor() == 0
            } catch (e: Exception) {
                false
            }

            if (!isInPath) {
                val notification = NotificationGroupManager.getInstance()
                    .getNotificationGroup("Coni Notifications")
                    .createNotification(
                        "Coni toolchain not found",
                        "The Coni language server is required for IDE features. Would you like to download it now?",
                        NotificationType.WARNING
                    )

                notification.addAction(object : AnAction("Download Coni") {
                    override fun actionPerformed(e: AnActionEvent) {
                        ConiDownloader.downloadBinary(project)
                        notification.expire()
                    }
                })

                notification.notify(project)
            }
        }
    }
}
