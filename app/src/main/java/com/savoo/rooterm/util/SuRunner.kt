package com.savoo.rooterm.util

import android.util.Log
import com.savoo.rooterm.data.OutputType
import com.savoo.rooterm.data.TerminalSession
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit

private const val TAG = "SuRunner"
private const val SENTINEL = "___RT_DONE___"

object SuRunner {

    fun initSession(session: TerminalSession): Boolean {
        return try {
            val proc = ProcessBuilder("su").redirectErrorStream(false).start()
            session.process = proc

            try {
                val pidField = proc.javaClass.getMethod("pid")
                session.suPid = pidField.invoke(proc) as Int
            } catch (_: Exception) {}

            session.writer = java.io.BufferedWriter(OutputStreamWriter(proc.outputStream))
            session.isAlive = true

            startReader(session, proc, false)
            startReader(session, proc, true)

            write(session, "id")
            write(session, "echo $SENTINEL")
            Thread.sleep(900)

            val allText = session.getAllOutputText()
            val rootCheck = allText.contains("uid=0") || allText.contains("root")
            session.isRoot = rootCheck
            session.removeLinesContaining("uid=", "gid=")

            if (!rootCheck) {
                session.addLine("✗ root denied", OutputType.ERROR)
                killSessionProcesses(session)
            }
            rootCheck
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
        if (!session.isAlive && session.process == null) return
        try { write(session, "exit") } catch (_: Exception) {}
        Thread.sleep(100)
        killSessionProcesses(session)
    }

    fun sendInterrupt(session: TerminalSession) {
        if (!session.isAlive) return
        val pid = session.suPid
        if (pid <= 0) return

        try {
            ProcessBuilder("su", "-c", "kill -2 -$pid")
                .redirectErrorStream(true)
                .start()
                .waitFor(2, TimeUnit.SECONDS)
        } catch (_: Exception) {}

        session.addLine("^C", OutputType.INFO)
    }

    private fun killSessionProcesses(session: TerminalSession) {
        val pid = session.suPid
        if (pid > 0) {
            try {
                ProcessBuilder("su", "-c", "kill -9 -$pid")
                    .redirectErrorStream(true)
                    .start()
                    .waitFor(2, TimeUnit.SECONDS)
            } catch (_: Exception) {}

            try {
                val pgid = getProcessGroupId(pid)
                if (pgid > 0 && pgid != pid) {
                    ProcessBuilder("su", "-c", "kill -9 -$pgid")
                        .redirectErrorStream(true)
                        .start()
                        .waitFor(2, TimeUnit.SECONDS)
                }
            } catch (_: Exception) {}
        }

        session.kill()
    }

    private fun getProcessGroupId(pid: Int): Int {
        return try {
            val proc = ProcessBuilder("su", "-c", "cat /proc/$pid/stat")
                .redirectErrorStream(true)
                .start()
            val output = proc.inputStream.bufferedReader().readText().trim()
            proc.waitFor(2, TimeUnit.SECONDS)
            val fields = output.split(" ")
            if (fields.size > 4) fields[4].toIntOrNull() ?: 0 else 0
        } catch (_: Exception) {
            0
        }
    }

    private fun write(session: TerminalSession, cmd: String) {
        val w = session.writer ?: throw IllegalStateException("no writer")
        synchronized(w) {
            w.write("$cmd\n")
            w.flush()
        }
    }

    private fun startReader(session: TerminalSession, proc: Process, isErr: Boolean) {
        val stream = if (isErr) proc.errorStream else proc.inputStream
        val type = if (isErr) OutputType.STDERR else OutputType.STDOUT
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
