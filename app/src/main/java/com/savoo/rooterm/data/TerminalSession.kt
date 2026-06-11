package com.savoo.rooterm.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.io.BufferedWriter
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

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

    @Volatile var process: Process? = null
    @Volatile var isRoot: Boolean = false
    @Volatile var isAlive: Boolean = false

    private val writerRef = AtomicReference<BufferedWriter?>(null)

    var writer: BufferedWriter?
        get() = writerRef.get()
        set(value) { writerRef.set(value) }

    @Volatile var lastRemoveHappened = false
    @Volatile var suPid: Int = 0
    @Volatile var isScrolling: Boolean = false
    @Volatile var scrollToBottom: Boolean = false

    private val pending = ArrayList<OutputLine>()
    private val lock = Any()
    private var lineSeq = 0L

    companion object {
        private const val MAX_PENDING = 5000
    }

    fun addLine(text: String, type: OutputType) {
        synchronized(lock) {
            if (pending.size >= MAX_PENDING) {
                val keep = pending.size - MAX_PENDING + 1
                val trimmed = ArrayList(pending.subList(keep, pending.size))
                pending.clear()
                pending.addAll(trimmed)
            }
            pending.add(OutputLine(text, type, "$id-${lineSeq++}"))
        }
    }

    fun flushPending(): Boolean {
        if (isScrolling) return false
        val batch: List<OutputLine>
        synchronized(lock) {
            if (pending.isEmpty()) return false
            val take = minOf(pending.size, 25)
            batch = ArrayList(pending.subList(0, take))
            pending.subList(0, take).clear()
        }
        output.addAll(batch)
        if (output.size > 1500) {
            val keep = output.subList(output.size - 1500, output.size).toList()
            output.clear()
            output.addAll(keep)
            lastRemoveHappened = true
        }
        return true
    }

    fun getAllOutputText(): String = synchronized(lock) {
        buildString {
            pending.forEach { append(it.text).append('\n') }
            output.forEach { append(it.text).append('\n') }
        }
    }

    fun removeLinesContaining(vararg patterns: String) {
        synchronized(lock) {
            pending.removeAll { line -> patterns.any { line.text.contains(it) } }
        }
        output.removeAll { line -> patterns.any { line.text.contains(it) } }
    }

    fun clear() {
        synchronized(lock) { pending.clear() }
        output.clear()
    }

    fun kill() {
        isAlive = false
        writerRef.getAndSet(null)?.let { try { it.close() } catch (_: Exception) {} }
        process?.let { proc ->
            try { proc.outputStream.close() } catch (_: Exception) {}
            try { proc.destroyForcibly() } catch (_: Exception) {}
        }
        process = null
    }
}
