package com.savoo.rooterm.util

import android.util.Log
import com.savoo.rooterm.data.OutputType
import com.savoo.rooterm.data.TerminalSession
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

private const val TAG      = "SuRunner"
private const val SENTINEL = "___RT_DONE___"

object SuRunner {

    fun initSession(session: TerminalSession): Boolean {
        return try {
            val proc = ProcessBuilder("su").redirectErrorStream(false).start()
            session.process = proc
            session.isAlive = true
            startReader(session, proc, false)
            startReader(session, proc, true)

            write(proc, "id")
            write(proc, "echo $SENTINEL")
            Thread.sleep(900)

            val ok = session.output.any { it.text.contains("uid=0") || it.text.contains("root") }
            session.isRoot = ok
            if (ok) {
                session.addLine("◆ root access granted", OutputType.INFO)
                write(proc, "export PS1='# '")
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
            write(session.process ?: return, cmd)
            write(session.process ?: return, "echo $SENTINEL")
        } catch (e: Exception) {
            session.addLine("error: ${e.message}", OutputType.ERROR)
        }
    }

    fun kill(session: TerminalSession) {
        try { write(session.process ?: return, "exit") } catch (_: Exception) {}
        session.kill()
    }

    private fun write(proc: Process, cmd: String) {
        BufferedWriter(OutputStreamWriter(proc.outputStream)).run {
            write("$cmd\n"); flush()
        }
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
