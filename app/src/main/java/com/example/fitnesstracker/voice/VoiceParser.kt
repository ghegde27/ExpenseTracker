package com.example.fitnesstracker.voice

import com.example.fitnesstracker.data.local.ExpenseCategory

data class VoiceTransaction(
    val type: Type,
    val amount: Long?,
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val clientName: String? = null,
    val source: String? = null,
    val description: String? = null,
    val rawText: String = ""
) {
    enum class Type { INCOME, EXPENSE, UNKNOWN }
}

object VoiceParser {

    private val expenseKeywords = listOf(
        "spent", "spend", "paid", "pay", "bought", "purchase", "purchased",
        "expense", "cost", "charged", "bill", "recharge", "fuel", "food",
        "equipment", "subscription", "swiggy", "zomato", "amazon", "netflix",
        "petrol", "diesel"
    )

    private val incomeKeywords = listOf(
        "received", "receive", "got", "earned", "salary", "income",
        "credited", "credit", "bonus", "payment received", "freelance", "refund"
    )

    private val digitRegex = Regex("""(?i)(?:₹|rs\.?|rupees?|inr)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""")

    private val wordToValue = mapOf(
        "zero" to 0L, "one" to 1L, "two" to 2L, "three" to 3L, "four" to 4L,
        "five" to 5L, "six" to 6L, "seven" to 7L, "eight" to 8L, "nine" to 9L,
        "ten" to 10L, "eleven" to 11L, "twelve" to 12L, "thirteen" to 13L,
        "fourteen" to 14L, "fifteen" to 15L, "sixteen" to 16L, "seventeen" to 17L,
        "eighteen" to 18L, "nineteen" to 19L, "twenty" to 20L, "thirty" to 30L,
        "forty" to 40L, "fifty" to 50L, "sixty" to 60L, "seventy" to 70L,
        "eighty" to 80L, "ninety" to 90L
    )

    fun parse(text: String): VoiceTransaction {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            return VoiceTransaction(VoiceTransaction.Type.UNKNOWN, null, rawText = text)
        }

        val lower = trimmed.lowercase()
        val amount = extractAmount(lower)
        val type = detectType(lower)
        val category = detectCategory(lower)
        val description = extractDescription(trimmed)
        val clientName = extractClientName(trimmed)
        val source = extractSource(trimmed)

        return VoiceTransaction(
            type = type,
            amount = amount,
            category = category,
            clientName = clientName,
            source = source ?: description,
            description = description,
            rawText = trimmed
        )
    }

    private fun detectType(lower: String): VoiceTransaction.Type {
        val hasExpense = expenseKeywords.any { lower.contains(it) }
        val hasIncome = incomeKeywords.any { lower.contains(it) }

        return when {
            hasExpense && !hasIncome -> VoiceTransaction.Type.EXPENSE
            hasIncome && !hasExpense -> VoiceTransaction.Type.INCOME
            hasExpense && hasIncome -> VoiceTransaction.Type.EXPENSE
            else -> VoiceTransaction.Type.UNKNOWN
        }
    }

    private fun detectCategory(lower: String): ExpenseCategory {
        return when {
            lower.contains("food") || lower.contains("lunch") || lower.contains("dinner") ||
                    lower.contains("swiggy") || lower.contains("zomato") || lower.contains("restaurant") ||
                    lower.contains("groceries") -> ExpenseCategory.FOOD

            lower.contains("fuel") || lower.contains("petrol") || lower.contains("diesel") ->
                ExpenseCategory.FUEL

            lower.contains("equipment") || lower.contains("gym equipment") || lower.contains("shoes") ||
                    lower.contains("machine") -> ExpenseCategory.EQUIPMENT

            lower.contains("netflix") || lower.contains("spotify") || lower.contains("subscription") ->
                ExpenseCategory.SUBSCRIPTION

            lower.contains("transport") || lower.contains("bus") || lower.contains("train") ||
                    lower.contains("cab") || lower.contains("uber") || lower.contains("ola") ->
                ExpenseCategory.PUBLIC_TRANSPORT

            lower.contains("phone") || lower.contains("recharge") || lower.contains("mobile") ->
                ExpenseCategory.PHONE

            else -> ExpenseCategory.OTHER
        }
    }

    private fun extractAmount(lower: String): Long? {
        // 1. Try finding explicit numbers (e.g. 500, ₹1,250, 25000)
        digitRegex.find(lower)?.let { match ->
            val numStr = match.groupValues[1].replace(",", "").trim()
            val d = numStr.toDoubleOrNull()
            if (d != null && d > 0) return d.toLong()
        }

        // 2. Try parsing spoken English / Indian words (e.g., "twenty five thousand", "one lakh")
        return parseSpokenWords(lower)
    }

    private fun parseSpokenWords(lower: String): Long? {
        val tokens = lower.replace("-", " ").split("\\s+".toRegex())
        var total = 0L
        var current = 0L
        var foundNumberWord = false

        for (token in tokens) {
            val valUnit = wordToValue[token]
            if (valUnit != null) {
                current += valUnit
                foundNumberWord = true
            } else when (token) {
                "hundred" -> {
                    current = (if (current == 0L) 1L else current) * 100L
                    foundNumberWord = true
                }
                "thousand" -> {
                    current = (if (current == 0L) 1L else current) * 1000L
                    total += current
                    current = 0L
                    foundNumberWord = true
                }
                "lakh", "lakhs" -> {
                    current = (if (current == 0L) 1L else current) * 100000L
                    total += current
                    current = 0L
                    foundNumberWord = true
                }
            }
        }
        total += current
        return if (foundNumberWord && total > 0) total else null
    }

    private fun extractDescription(text: String): String? {
        val lower = text.lowercase()

        // "at Swiggy", "at Amazon"
        Regex("""(?i)\bat\s+([A-Za-z0-9 .&'-]+)""").find(text)?.let {
            val res = cleanPhrase(it.groupValues[1])
            if (res.isNotBlank()) return res
        }

        // "to Amazon"
        Regex("""(?i)\bto\s+([A-Za-z0-9 .&'-]+)""").find(text)?.let {
            val res = cleanPhrase(it.groupValues[1])
            if (res.isNotBlank()) return res
        }

        // "from Ravi", "from freelance work"
        Regex("""(?i)\bfrom\s+([A-Za-z0-9 .&'-]+)""").find(text)?.let {
            val res = cleanPhrase(it.groupValues[1])
            if (res.isNotBlank()) return "From $res"
        }

        // "on food", "on Netflix", "for fuel"
        Regex("""(?i)\b(?:on|for)\s+([A-Za-z0-9 .&'-]+)""").find(text)?.let {
            val res = cleanPhrase(it.groupValues[1])
            if (res.isNotBlank()) return res.replaceFirstChar { c -> c.uppercase() }
        }

        // Specific keywords
        return when {
            lower.contains("salary") -> "Salary"
            lower.contains("freelance") -> "Freelance work"
            lower.contains("bonus") -> "Bonus"
            lower.contains("petrol") || lower.contains("fuel") -> "Fuel"
            lower.contains("lunch") -> "Lunch"
            lower.contains("food") -> "Food"
            lower.contains("equipment") -> "Equipment"
            else -> null
        }
    }

    private fun extractClientName(text: String): String? {
        Regex("""(?i)\bfrom\s+([A-Za-z0-9 .&'-]+)""").find(text)?.let {
            val candidate = cleanPhrase(it.groupValues[1])
            if (candidate.isNotBlank() && !candidate.equals("freelance work", ignoreCase = true)) {
                return candidate.replaceFirstChar { c -> c.uppercase() }
            }
        }
        return null
    }

    private fun extractSource(text: String): String? {
        Regex("""(?i)\b(?:at|to)\s+([A-Za-z0-9 .&'-]+)""").find(text)?.let {
            val candidate = cleanPhrase(it.groupValues[1])
            if (candidate.isNotBlank()) return candidate.replaceFirstChar { c -> c.uppercase() }
        }
        return null
    }

    private fun cleanPhrase(phrase: String): String {
        return phrase
            .replace(Regex("""(?i)\b(?:expense|income|rupees|rs|for|on)\b.*"""), "")
            .trim()
            .trim('.', ',', ':', '-')
    }
}
