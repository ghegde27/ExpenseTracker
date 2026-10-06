package com.example.fitnesstracker.sms

data class ExpenseSuggestion(val amount: Long, val source: String, val dateText: String?, val rawPreview: String)

object SmsExpenseParser {
    private val money = Regex("(?:INR|Rs\\.?|₹)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)", RegexOption.IGNORE_CASE)
    fun parse(message: String): ExpenseSuggestion? {
        if (!(message.contains("debited", true) || message.contains("spent", true) || message.contains("purchase", true))) return null
        val amount = money.find(message)?.groupValues?.getOrNull(1)?.replace(",", "")?.toDoubleOrNull()?.toLong() ?: return null
        val source = Regex("(?:at|to|from)\\s+([A-Za-z0-9 .&_-]{2,60})", RegexOption.IGNORE_CASE).find(message)?.groupValues?.getOrNull(1)?.trim() ?: "Bank transaction"
        return ExpenseSuggestion(amount, source, null, message.take(120))
    }
}
