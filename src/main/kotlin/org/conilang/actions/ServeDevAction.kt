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

class ServeDevAction : AnAction("Serve Coni Playground (Dev)") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val dir = virtualFile?.parent?.path ?: project.basePath ?: "/"
        
        val commandLine = com.intellij.execution.configurations.GeneralCommandLine("coni", "serve", "--dev", dir, "8080")
        commandLine.workDirectory = java.io.File(dir)

        try {
            val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
            ProcessTerminatedListener.attach(processHandler)

            val consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project)
            val consoleView = consoleBuilder.console
            consoleView.attachToProcess(processHandler)

            val executor = DefaultRunExecutor.getRunExecutorInstance()
            val descriptor = RunContentDescriptor(consoleView, processHandler, consoleView.component, "Coni Playground (Port 8080)")
            RunContentManager.getInstance(project).showRunContent(executor, descriptor)

            processHandler.startNotify()
            
            // Optionally open the browser automatically to localhost:8080
            // com.intellij.ide.BrowserUtil.browse("http://localhost:8080")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
