package com.savoo.rooterm.util

import android.util.Log
import com.savoo.rooterm.data.OutputType
import com.savoo.rooterm.data.TerminalSession
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

private const val TAG      = "SuRunner"
private const val SENTINEL = "___RT_DONE___"

object SuRunner {

    fun initSession(session: TerminalSession): Boolean {
        return try {
            val proc = ProcessBuilder("su").redirectErrorStream(false).start()
            session.process = proc
            session.writer = java.io.BufferedWriter(OutputStreamWriter(proc.outputStream))
            session.isAlive = true
            startReader(session, proc, false)
            startReader(session, proc, true)

            write(session, "id")
            write(session, "echo $SENTINEL")
            Thread.sleep(900)

            val ok = session.output.any { it.text.contains("uid=0") || it.text.contains("root") }
            session.isRoot = ok
            if (ok) {
                session.addLine("◆ root access granted", OutputType.INFO)
                write(session, "export PS1='# '")
            } else {
                session.addLine("✗ root denied", OutputType.ERROR)
                proc.destroy(); session.isAlive = false
            }
            ok
        } catch (e: Exception) {
            Log.e(TAG, "init failed", e)
            session.addLine("error: ${e.message}", OutputType.ERROR)
            false
        }
    }

    fun send(session: TerminalSession, cmd: String) {
        if (cmd.isBlank() || !session.isAlive) return
        session.addLine("# $cmd", OutputType.COMMAND)
        try {
            write(session, cmd)
            write(session, "echo $SENTINEL")
        } catch (e: Exception) {
            session.addLine("error: ${e.message}", OutputType.ERROR)
        }
    }

    fun kill(session: TerminalSession) {
        try { write(session, "exit") } catch (_: Exception) {}
        session.kill()
    }

    fun sendInterrupt(session: TerminalSession) {
        if (!session.isAlive) return
        val proc = session.process ?: return

        try {
            val suPid = proc.javaClass.getMethod("pid").invoke(proc) as Int
            ProcessBuilder("su", "-c", "kill -2 -- -$suPid")
                .redirectErrorStream(true)
                .start()
                .waitFor(2, java.util.concurrent.TimeUnit.SECONDS)
        } catch (_: Exception) {}

        try {
            proc.outputStream.write(3)
            proc.outputStream.flush()
        } catch (_: Exception) {}

        session.addLine("^C", OutputType.INFO)
    }

    private fun write(session: TerminalSession, cmd: String) {
        val w = session.writer ?: throw IllegalStateException("no writer")
        w.write("$cmd\n")
        w.flush()
    }

    private fun startReader(session: TerminalSession, proc: Process, isErr: Boolean) {
        val stream = if (isErr) proc.errorStream else proc.inputStream
        val type   = if (isErr) OutputType.STDERR else OutputType.STDOUT
        Thread {
            try {
                BufferedReader(InputStreamReader(stream)).forEachLine { line ->
                    if (line.trimEnd() != SENTINEL) session.addLine(line, type)
                }
            } catch (e: Exception) {
                if (session.isAlive) session.addLine("[stream: ${e.message}]", OutputType.ERROR)
            }
            session.isAlive = false
        }.also { it.isDaemon = true; it.name = "rt-${session.id}-${if (isErr) "err" else "out"}" }.start()
    }
}
