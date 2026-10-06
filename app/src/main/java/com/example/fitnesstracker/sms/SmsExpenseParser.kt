package com.example.fitnesstracker.sms

data class ExpenseSuggestion(
    val amount: Long,
    val source: String,
    val dateText: String?,
    val rawPreview: String
)

object SmsExpenseParser {

    // ---------------------------------------------------------
    // Ignore keywords for non-expense messages
    // ---------------------------------------------------------
    private val ignoreKeywords = listOf(
        "credited",
        "credit",
        "refund",
        "refunded",
        "cashback",
        "cash back",
        "reversed",
        "reversal",
        "failed",
        "declined",
        "otp",
        "one time password",
        "available balance",
        "account balance",
        "closing balance",
        "avl bal",
        "bal is",
        "balance is",
        "bal:"
    )

    // ---------------------------------------------------------
    // Keywords indicating a debit/payment action
    // Note: Provider/Bank alone is NOT enough; one of these action
    // keywords must be present.
    // ---------------------------------------------------------
    private val expenseActionKeywords = listOf(
        "debited",
        "debit",
        "spent",
        "spend",
        "purchase",
        "purchased",
        "paid",
        "payment",
        "transferred",
        "transfer",
        "withdrawn",
        "withdrawal",
        "Sent"
    )

    // ---------------------------------------------------------
    // Regex Patterns for Amount
    // ---------------------------------------------------------

    // ₹1,250 | INR 1,250 | Rs. 1,250 | Rs 1250
    private val currencyAmount = Regex(
        """(?i)(?:INR|Rs\.?|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)"""
    )

    // 1,250 INR | 1,250 Rs
    private val amountBeforeCurrency = Regex(
        """(?i)([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:INR|Rs\.?|₹)"""
    )

    // debited by 1250 | spent 1250
    private val actionAmount = Regex(
        """(?i)\b(?:debited|debit|spent|spend|paid|payment|purchase|purchased|transferred)\s+(?:by|of|for|worth)?\s*(?:INR|Rs\.?|₹)?\s*([0-9,]+(?:\.[0-9]{1,2})?)"""
    )

    // transaction of 1250 | txn of ₹1250
    private val transactionOfAmount = Regex(
        """(?i)\b(?:transaction|txn)\s+(?:of|for|worth)\s*(?:INR|Rs\.?|₹)?\s*([0-9,]+(?:\.[0-9]{1,2})?)"""
    )

    // ---------------------------------------------------------
    // Merchant / Recipient Patterns
    // ---------------------------------------------------------
    private val merchantPatterns = listOf(
        // paid to / spent at / at / to / from
        Regex("""(?i)\bpaid\s+(?:to|at)\s+([A-Za-z0-9][A-Za-z0-9 .&'_-]{1,40}?)(?=\s+(?:on|using|via|with|for|ref|txn|transaction|upi|card|through|a/c|ac|ending|was)\b|[.,;]|$)"""),
        Regex("""(?i)\bspent\s+at\s+([A-Za-z0-9][A-Za-z0-9 .&'_-]{1,40}?)(?=\s+(?:on|using|via|with|for|ref|txn|transaction|upi|card|through)\b|[.,;]|$)"""),
        Regex("""(?i)\bat\s+([A-Za-z0-9][A-Za-z0-9 .&'_-]{1,40}?)(?=\s+(?:on|using|via|with|for|ref|txn|transaction|upi|card|from|through|was)\b|[.,;]|$)"""),
        Regex("""(?i)\bto\s+([A-Za-z0-9][A-Za-z0-9 .&'_-]{1,40}?)(?=\s+(?:on|using|via|with|for|ref|txn|transaction|upi|card|from|through|was)\b|[.,;]|$)"""),
        Regex("""(?i)\bmerchant\s*[:\-]\s*([A-Za-z0-9][A-Za-z0-9 .&'_-]{1,40})"""),
        Regex("""(?i)\bvpa\s*[:\-]?\s*([A-Za-z0-9._@-]{2,40})""")
    )

    fun parse(message: String): ExpenseSuggestion? {
        val normalized = normalize(message)
        if (normalized.isBlank()) return null

        // 1. Must NOT contain ignore keywords (refund, OTP, balance check, etc.)
        if (shouldIgnore(normalized)) return null

        // 2. Must contain explicit debit/payment action
        if (!hasDebitAction(normalized)) return null

        // 3. Must extract a valid amount > 0
        val amount = extractAmount(normalized) ?: return null
        if (amount <= 0) return null

        // 4. Extract merchant/source name
        val source = extractSource(normalized)

        return ExpenseSuggestion(
            amount = amount,
            source = source,
            dateText = null,
            rawPreview = normalized.take(120)
        )
    }

    private fun normalize(message: String): String {
        return message
            .replace("\n", " ")
            .replace("\r", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun shouldIgnore(message: String): Boolean {
        val lower = message.lowercase()
        return ignoreKeywords.any { keyword ->
            lower.contains(keyword)
        }
    }

    private fun hasDebitAction(message: String): Boolean {
        val lower = message.lowercase()
        // Must contain at least one expense action keyword
        return expenseActionKeywords.any { keyword ->
            Regex("""\b${Regex.escape(keyword)}\b""", RegexOption.IGNORE_CASE).containsMatchIn(lower)
        }
    }

    private fun extractAmount(message: String): Long? {
        // 1. Currency followed by amount: ₹1,250 | INR 1,250 | Rs 450
        currencyAmount.find(message)?.let { match ->
            return parseNumber(match.groupValues[1])
        }

        // 2. Amount followed by currency: 1,250 INR
        amountBeforeCurrency.find(message)?.let { match ->
            return parseNumber(match.groupValues[1])
        }

        // 3. Action + amount
        actionAmount.find(message)?.let { match ->
            return parseNumber(match.groupValues[1])
        }

        // 4. Transaction of amount
        transactionOfAmount.find(message)?.let { match ->
            return parseNumber(match.groupValues[1])
        }

        return null
    }

    private fun parseNumber(value: String): Long? {
        val cleaned = value.replace(",", "").trim()
        val d = cleaned.toDoubleOrNull() ?: return null
        return d.toLong()
    }

    private fun extractSource(message: String): String {
        // Try merchant extraction patterns
        for (pattern in merchantPatterns) {
            val match = pattern.find(message)
            if (match != null) {
                val candidate = match.groupValues.getOrNull(1)?.trim()?.trim('.', ',', ':', '-', '/')
                if (!candidate.isNullOrBlank() && !isPaymentProvider(candidate) && isValidMerchantName(candidate)) {
                    return cleanSource(candidate)
                }
            }
        }

        // Fallback to bank or payment app name if found
        findPaymentProvider(message)?.let {
            return it
        }

        return "Bank SMS"
    }

    private fun isValidMerchantName(name: String): Boolean {
        val lower = name.lowercase()
        val invalidTokens = listOf("your", "account", "a/c", "ac", "bank", "card", "rs", "inr", "upi", "successful", "debited", "paid")
        return invalidTokens.none { lower == it }
    }

    private fun findPaymentProvider(message: String): String? {
        val lower = message.lowercase()
        return when {
            lower.contains("phonepe") || lower.contains("phone pe") -> "PhonePe"
            lower.contains("gpay") || lower.contains("google pay") || lower.contains("googlepay") -> "Google Pay"
            lower.contains("paytm") -> "Paytm"
            lower.contains("hdfc") -> "HDFC Bank"
            lower.contains("icici") -> "ICICI Bank"
            else -> null
        }
    }

    private fun isPaymentProvider(value: String): Boolean {
        val norm = value.lowercase().replace(" ", "").trim()
        return norm.contains("phonepe") || norm.contains("gpay") || norm.contains("googlepay") ||
                norm.contains("paytm") || norm.contains("hdfc") || norm.contains("icici")
    }

    private fun cleanSource(source: String): String {
        return source
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(50)
    }
}
