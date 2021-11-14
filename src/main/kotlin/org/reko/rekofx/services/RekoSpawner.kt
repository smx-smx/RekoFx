package org.reko.rekofx.services

import java.nio.file.Paths

class RekoSpawner(private val exePathName: String) {
    fun start(): RekoSession {
        val exePath = Paths.get(exePathName)
        val exeFile = exePath.toFile()

        val reko = ProcessBuilder(exePathName)
            .also {
                //it.redirectErrorStream(true)
                it.directory(exeFile.parentFile)
            }.start()

        return RekoSession(reko)
    }
}