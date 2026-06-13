package com.savoo.rooterm.util

import android.util.Log
import com.savoo.rooterm.data.OutputType
import com.savoo.rooterm.data.TerminalSession
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit

private const val TAG = "SuRunner"

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
        session.suppressOutput.value = false
        session.isCommandRunning.value = true
        session.autoScroll = true
        session.lastOutputTime = System.currentTimeMillis()
        session.addLine("# $cmd", OutputType.COMMAND)
        try {
            write(session, cmd)
        } catch (e: Exception) {
            session.isCommandRunning.value = false
            session.addLine("error: ${e.message}", OutputType.ERROR)
        }
    }

    fun sendWithCheck(session: TerminalSession, cmd: String, blockDangerous: Boolean): Boolean {
        if (blockDangerous && isDangerous(cmd)) {
            session.addLine("# $cmd", OutputType.COMMAND)
            session.addLine("dangerous command blocked. disable dangerous command protection to force execution if you know what you are doing.", OutputType.ERROR)
            return false
        }
        send(session, cmd)
        return true
    }

    private val DANGEROUS_PATTERNS = listOf(
        Regex("""rm\s+(-[a-zA-Z]*\s+)*/\b"""),
        Regex("""rm\s+(-[a-zA-Z]*\s+)*/\*"""),
        Regex("""mkfs"""),
        Regex("""dd\s+.*of=/dev/"""),
        Regex("""chmod\s+777\s+/"""),
        Regex("""chown\s+root\s+/"""),
        Regex("""format"""),
        Regex("""fastboot\s+erase"""),
        Regex(""":\(\)\s*\{"""),
        Regex("""mv\s+/\*\s+/dev/null"""),
        Regex("""rm\s+-[a-zA-Z]*r[a-zA-Z]*f[a-zA-Z]*\s+/\s"""),
        Regex("""rm\s+-[a-zA-Z]*f[a-zA-Z]*r[a-zA-Z]*\s+/\s"""),
    )

    fun isDangerous(cmd: String): Boolean {
        val trimmed = cmd.trim()
        return DANGEROUS_PATTERNS.any { it.containsMatchIn(trimmed) }
    }

    fun stopCommand(session: TerminalSession) {
        if (!session.isCommandRunning.value) return
        session.isCommandRunning.value = false
        session.suppressOutput.value = true

        val oldProc = session.process
        session.isAlive = false

        try { oldProc?.outputStream?.close() } catch (_: Exception) {}
        try { oldProc?.inputStream?.close() } catch (_: Exception) {}
        try { oldProc?.errorStream?.close() } catch (_: Exception) {}

        val pid = session.suPid
        if (pid > 0) {
            try {
                ProcessBuilder("su", "-c", "kill -9 -$pid")
                    .redirectErrorStream(true)
                    .start()
                    .waitFor(1, TimeUnit.SECONDS)
            } catch (_: Exception) {}
        }

        Thread.sleep(200)

        val cutAt = session.output.size
        session.output.subList(cutAt, session.output.size).clear()
        session.addLine("^C", OutputType.INFO)

        try {
            val proc = ProcessBuilder("su").redirectErrorStream(false).start()
            session.process = proc
            try {
                val pidField = proc.javaClass.getMethod("pid")
                session.suPid = pidField.invoke(proc) as Int
            } catch (_: Exception) {}
            session.writer = java.io.BufferedWriter(OutputStreamWriter(proc.outputStream))
            session.isAlive = true
            session.suppressOutput.value = false
            startReader(session, proc, false)
            startReader(session, proc, true)
        } catch (_: Exception) {}
    }

    fun kill(session: TerminalSession) {
        if (!session.isAlive && session.process == null) return
        try { write(session, "exit") } catch (_: Exception) {}
        Thread.sleep(100)
        killSessionProcesses(session)
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
                val reader = BufferedReader(InputStreamReader(stream))
                while (true) {
                    val line = reader.readLine() ?: break
                    if (session.suppressOutput.value) break
                    session.lastOutputTime = System.currentTimeMillis()
                    session.addLine(line, type)
                }
            } catch (e: Exception) {
                if (session.isAlive) session.addLine("[stream: ${e.message}]", OutputType.ERROR)
            }
            session.isAlive = false
            session.isCommandRunning.value = false
        }.also { it.isDaemon = true; it.name = "rt-${session.id}-${if (isErr) "err" else "out"}" }.start()
    }
}
