package com.example.lyrics.model

enum class LyricRole {
    MAIN_VOCAL,
    BACKGROUND_VOCAL
}

data class LyricSinger(
    val id: String,
    val name: String
)

data class LyricWord(
    val globalIndex: Int,
    val text: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val lineIndex: Int,
    val role: LyricRole = LyricRole.MAIN_VOCAL,
    val singer: LyricSinger? = null,
    val isBackgroundVocal: Boolean = false
) {
    val durationMs: Long get() = (endTimeMs - startTimeMs).coerceAtLeast(0L)

    fun progressAt(currentPositionMs: Long): Float {
        if (currentPositionMs <= startTimeMs) return 0f
        if (currentPositionMs >= endTimeMs) return 1f
        val duration = durationMs
        if (duration <= 0L) return 1f
        return ((currentPositionMs - startTimeMs).toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    }
}

data class LyricLine(
    val lineIndex: Int,
    val text: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val words: List<LyricWord> = emptyList(),
    val role: LyricRole = LyricRole.MAIN_VOCAL,
    val singer: LyricSinger? = null,
    val isBackgroundVocal: Boolean = false
) {
    val durationMs: Long get() = (endTimeMs - startTimeMs).coerceAtLeast(0L)
    val hasWordTiming: Boolean get() = words.isNotEmpty()

    fun isActiveAt(currentPositionMs: Long): Boolean {
        return currentPositionMs in startTimeMs..endTimeMs
    }

    fun isPast(currentPositionMs: Long): Boolean {
        return currentPositionMs > endTimeMs
    }

    fun isFuture(currentPositionMs: Long): Boolean {
        return currentPositionMs < startTimeMs
    }
}

data class LyricDocument(
    val songId: String,
    val title: String = "",
    val artist: String = "",
    val lines: List<LyricLine> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val source: String = "unknown",
    val hasWordTiming: Boolean = false
) {
    val isEmpty: Boolean get() = lines.isEmpty()

    /**
     * Binary search to find the active line index at time [positionMs] in O(log N).
     * If between lines (during silence), returns the line that just ended or upcoming line based on proximity.
     */
    fun findActiveLineIndex(positionMs: Long): Int {
        if (lines.isEmpty()) return -1
        if (positionMs < lines.first().startTimeMs) return 0
        if (positionMs >= lines.last().endTimeMs) return lines.lastIndex

        var low = 0
        var high = lines.lastIndex
        var bestIndex = -1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val line = lines[mid]

            when {
                positionMs in line.startTimeMs..line.endTimeMs -> return mid
                positionMs < line.startTimeMs -> {
                    bestIndex = mid
                    high = mid - 1
                }
                else -> {
                    low = mid + 1
                }
            }
        }

        return (bestIndex - 1).coerceAtLeast(0)
    }

    /**
     * Binary search to find the active word index within a line at time [positionMs] in O(log W).
     */
    fun findActiveWordIndex(line: LyricLine, positionMs: Long): Int {
        val words = line.words
        if (words.isEmpty()) return -1
        if (positionMs < words.first().startTimeMs) return -1
        if (positionMs >= words.last().endTimeMs) return words.size // all sung

        var low = 0
        var high = words.lastIndex
        while (low <= high) {
            val mid = (low + high) ushr 1
            val word = words[mid]
            when {
                positionMs in word.startTimeMs..word.endTimeMs -> return mid
                positionMs < word.startTimeMs -> high = mid - 1
                else -> low = mid + 1
            }
        }
        return low.coerceIn(0, words.size)
    }
}
