package org.reko.rekofx.gui

import com.airhacks.afterburner.injection.Injector
import javafx.application.Application
import javafx.application.HostServices
import javafx.scene.Scene
import javafx.stage.Stage
import org.reko.rekofx.services.RekoSession
import org.reko.rekofx.services.RekoSpawner
import kotlin.system.exitProcess

class MainApp : Application() {

    private fun startDecompileDaemon(): RekoSession {
        val rekoDaemonPath = parameters.raw.firstOrNull()
        if(rekoDaemonPath == null){
            println("Usage: <path to decompiled.exe>")
            exitProcess(1)
        }

        val reko = RekoSpawner(rekoDaemonPath).start()
        return reko
    }

    override fun start(primaryStage: Stage) {
        val reko = startDecompileDaemon()
        Runtime.getRuntime().addShutdownHook(Thread {
            reko.close()
        })

        Injector.setModelOrService(HostServices::class.java, hostServices)
        Injector.setModelOrService(Stage::class.java, primaryStage)
        Injector.setModelOrService(RekoSession::class.java, reko)
        initRootLayout()
    }

    private fun initRootLayout() {
        val root = MainView()
        val stage = Stage().apply {
            title = "Reko Decompiler"
            scene = Scene(root.view)
        }
        stage.show()
    }

    companion object {
        @JvmStatic
        fun main(args:Array<String>){
            launch(MainApp::class.java, *args)
        }
    }
}

class EntryPoint {
    companion object {
        @JvmStatic
        fun main(args:Array<String>){
            MainApp.main(args)
        }
    }
}