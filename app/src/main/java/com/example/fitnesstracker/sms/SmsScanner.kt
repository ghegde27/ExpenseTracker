package com.example.fitnesstracker.sms

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.fitnesstracker.data.local.ExpenseCategory
import com.example.fitnesstracker.data.local.ExpenseEntity
import com.example.fitnesstracker.data.local.PaymentMethod
import com.example.fitnesstracker.domain.FinanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsScanner @Inject constructor(
    private val financeRepository: FinanceRepository
) {

    private fun getTodaySmsSelection(): Pair<String, Array<String>> {

        val calendar = Calendar.getInstance()

        calendar.set(
            Calendar.HOUR_OF_DAY,
            0
        )
        calendar.set(
            Calendar.MINUTE,
            0
        )
        calendar.set(
            Calendar.SECOND,
            0
        )
        calendar.set(
            Calendar.MILLISECOND,
            0
        )

        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)

        val startOfTomorrow = calendar.timeInMillis

        return Pair(
            "date >= ? AND date < ?",
            arrayOf(
                startOfDay.toString(),
                startOfTomorrow.toString()
            )
        )
    }

    suspend fun scanInbox(context: Context): ScanResult = withContext(Dispatchers.IO) {
        var scannedCount = 0
        var addedCount = 0

        val (selection, selectionArgs) = getTodaySmsSelection()

        try {
            val inboxUri = Uri.parse("content://sms/inbox")
            val projection = arrayOf("address", "body", "date")

            val cursor = context.contentResolver.query(
                inboxUri,
                projection,
                selection,
                selectionArgs,
                "date DESC"
            )

            cursor?.use { c ->
                val bodyIdx = c.getColumnIndex("body")
                val dateIdx = c.getColumnIndex("date")

                while (c.moveToNext()) {
                    val body = if (bodyIdx >= 0) c.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx >= 0) c.getLong(dateIdx) else System.currentTimeMillis()

                    if (body.isBlank()) continue
                    scannedCount++

                    val suggestion = SmsExpenseParser.parse(body) ?: continue

                    val description = if (suggestion.source != "Bank SMS" && suggestion.source != "Bank transaction") {
                        "${suggestion.source} (SMS detected)"
                    } else {
                        "Bank SMS detected"
                    }

                    val expense = ExpenseEntity(
                        amount = suggestion.amount,
                        category = ExpenseCategory.OTHER,
                        expenseDate = if (date > 0) date else System.currentTimeMillis(),
                        source = suggestion.source,
                        description = description,
                        paymentMethod = PaymentMethod.UPI,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    val inserted = financeRepository.insertExpenseIfNotDuplicate(expense)
                    if (inserted) {
                        addedCount++
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning SMS inbox", e)
            return@withContext ScanResult(
                scannedCount = scannedCount,
                addedCount = addedCount,
                errorMessage = e.localizedMessage ?: "Failed to scan SMS inbox"
            )
        }

        return@withContext ScanResult(
            scannedCount = scannedCount,
            addedCount = addedCount,
            errorMessage = null
        )
    }

    companion object {
        private const val TAG = "SmsScanner"
    }
}

data class ScanResult(
    val scannedCount: Int,
    val addedCount: Int,
    val errorMessage: String?
)
