package org.conilang.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.openapi.ui.Messages
import org.conilang.util.ConiExecutable
import org.conilang.util.ConiDownloader

class RunScriptAction : AnAction("Run Coni Script") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        if (virtualFile.extension != "coni") return

        val filePath = virtualFile.path
        val coniExe = ConiExecutable.resolve(project)
        val commandLine = com.intellij.execution.configurations.GeneralCommandLine(coniExe, filePath)
        commandLine.workDirectory = java.io.File(project.basePath ?: "/")

        try {
            val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
            ProcessTerminatedListener.attach(processHandler)

            val consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project)
            val consoleView = consoleBuilder.console
            consoleView.attachToProcess(processHandler)

            val executor = DefaultRunExecutor.getRunExecutorInstance()
            val descriptor = RunContentDescriptor(consoleView, processHandler, consoleView.component, "Coni: ${virtualFile.name}")
            RunContentManager.getInstance(project).showRunContent(executor, descriptor)

            processHandler.startNotify()
        } catch (ex: Exception) {
            val res = Messages.showYesNoDialog(
                project,
                "Failed to execute Coni: ${ex.message}\n\nWould you like to download the Coni language server?",
                "Execution Failed",
                "Download",
                "Cancel",
                Messages.getErrorIcon()
            )
            if (res == Messages.YES) {
                ConiDownloader.downloadBinary(project)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = virtualFile != null && virtualFile.extension == "coni"
    }
}
