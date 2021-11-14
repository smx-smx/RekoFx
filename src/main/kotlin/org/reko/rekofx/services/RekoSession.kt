package org.reko.rekofx.services

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.OutputStreamWriter
import kotlin.concurrent.thread

@Serializable
data class RekoEventLocation(val program: String, val addr: String)

@Serializable
data class RekoEvent(val type: String, val location: String, val message: String){

}

typealias EventListenerFunc = (event:RekoEvent) -> Unit

class RekoSession(private val proc: Process) : AutoCloseable {
    val newline = System.lineSeparator()

    val stdin = OutputStreamWriter(proc.outputStream)
    val stdout = proc.inputStream.bufferedReader().lines()
    val stderr = proc.errorStream.bufferedReader().lines()

    val outLines = stdout.iterator()
    val errLines = stderr.iterator()

    private val listeners: ArrayList<EventListenerFunc> = ArrayList()

    private val eventListener = Thread {
        while(!Thread.interrupted()) {
            val json = kotlin.runCatching { errLines.next() }
                .getOrNull() ?: break
            println(json)
            val ev = Json.decodeFromString(json) as RekoEvent
            listeners.forEach { it(ev) }
        }
    }.also {
        it.isDaemon = true
        it.start()
    }

    fun addEventListener(cb:EventListenerFunc) {
        listeners.add(cb)
    }

    inline fun <reified Tin> sendCommand(cmd: Tin): String {
        val jsonIn = Json.encodeToString(cmd)
        println("Sending ${jsonIn}")
        stdin.write(jsonIn + newline)
        stdin.flush()
        return outLines.next()
    }

    private fun sendCloseMessage(){
        val cmd = buildJsonObject {
            put("cmd", "exit")
        }
        sendCommand(cmd)
    }

    override fun close() {
        // close stderr to stop iteration
        stderr.close()

        sendCloseMessage()
        proc.waitFor()

        stdout.close()
        stdin.close()
    }
}