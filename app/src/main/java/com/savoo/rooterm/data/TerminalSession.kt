package com.savoo.rooterm.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
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

    fun addLine(text: String, type: OutputType) {
        output.add(OutputLine(text, type))
        if (output.size > 4000) output.removeRange(0, output.size - 4000)
    }

    fun clear() = output.clear()

    fun kill() {
        process?.destroy()
        isAlive = false
    }
}
