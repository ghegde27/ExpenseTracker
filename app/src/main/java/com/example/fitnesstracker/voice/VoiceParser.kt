package com.example.fitnesstracker.voice

data class VoiceTransaction(val type: Type, val amount: Long?, val clientName: String? = null, val source: String? = null) { enum class Type { INCOME, EXPENSE, UNKNOWN } }

object VoiceParser {
    private val amountRegex = Regex("(?:₹|rs\\.?|rupees?)\\s*(\\d+(?:\\.\\d+)?)", RegexOption.IGNORE_CASE)
    private val numberWords = mapOf("one" to 1L, "two" to 2L, "three" to 3L, "four" to 4L, "five" to 5L, "six" to 6L, "seven" to 7L, "eight" to 8L, "nine" to 9L, "ten" to 10L, "hundred" to 100L, "thousand" to 1000L)
    fun parse(text: String): VoiceTransaction {
        val amount = amountRegex.find(text)?.groupValues?.getOrNull(1)?.toDoubleOrNull()?.toLong() ?: parseSpokenAmount(text)
        val lower = text.lowercase()
        return when {
            lower.contains("income") -> VoiceTransaction(VoiceTransaction.Type.INCOME, amount, clientName = afterFrom(text))
            lower.contains("expense") || lower.contains("petrol") || lower.contains("fuel") -> VoiceTransaction(VoiceTransaction.Type.EXPENSE, amount, source = afterExpenseSource(text))
            else -> VoiceTransaction(VoiceTransaction.Type.UNKNOWN, amount)
        }
    }
    private fun parseSpokenAmount(text: String): Long? {
        val tokens = text.lowercase().replace("-", " ").split("\\s+".toRegex())
        val idx = tokens.indexOfFirst { it in numberWords.keys }
        if (idx < 0) return null
        var total = 0L; var current = 0L
        for (i in idx until tokens.size) { val n=numberWords[tokens[i]] ?: break; if (n==100L || n==1000L) current = (if(current==0L) 1 else current)*n else current += n; if(tokens[i] == "rupees" || tokens[i] == "income" || tokens[i] == "expense") break }
        total += current
        return total.takeIf { it > 0 }
    }
    private fun afterFrom(text: String) = Regex("from\\s+(.+)$", RegexOption.IGNORE_CASE).find(text)?.groupValues?.getOrNull(1)?.trim()
    private fun afterExpenseSource(text: String) = Regex("(?:expense|petrol|fuel)\\s*(?:at|for)?\\s*(.*)$", RegexOption.IGNORE_CASE).find(text)?.groupValues?.getOrNull(1)?.trim()?.ifBlank { null }
}
