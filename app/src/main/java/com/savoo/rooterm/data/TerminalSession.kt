package com.savoo.rooterm.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.io.BufferedWriter
import java.util.UUID

enum class OutputType { COMMAND, STDOUT, STDERR, INFO, ERROR }

data class OutputLine(
    val text: String,
    val type: OutputType,
    val id: String = UUID.randomUUID().toString(),
)

class TerminalSession(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "shell",
) {
    val output: SnapshotStateList<OutputLine> = mutableStateListOf()
    var process: Process? = null
    var isRoot: Boolean = false
    var isAlive: Boolean = false
    var writer: BufferedWriter? = null

    @Volatile var lastRemoveHappened = false

    private val pending = ArrayList<OutputLine>()
    private val lock = Any()
    private var lineSeq = 0L

    fun addLine(text: String, type: OutputType) {
        synchronized(lock) {
            pending.add(OutputLine(text, type, "$id-${lineSeq++}"))
        }
    }

    fun flushPending(): Boolean {
        val batch: List<OutputLine>
        synchronized(lock) {
            if (pending.isEmpty()) return false
            val take = minOf(pending.size, 25)
            batch = ArrayList(pending.subList(0, take))
            pending.subList(0, take).clear()
        }
        output.addAll(batch)
        if (output.size > 1500) {
            val trimTo = output.size - 1500
            output.removeRange(0, trimTo)
            lastRemoveHappened = true
        }
        return true
    }

    fun clear() {
        synchronized(lock) { pending.clear() }
        output.clear()
    }

    fun kill() {
        writer?.let { try { it.close() } catch (_: Exception) {} }
        writer = null
        process?.destroy()
        isAlive = false
    }
}
