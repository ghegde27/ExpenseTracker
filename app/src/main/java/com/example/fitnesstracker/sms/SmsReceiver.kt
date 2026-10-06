package com.example.fitnesstracker.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.fitnesstracker.data.local.ExpenseCategory
import com.example.fitnesstracker.data.local.ExpenseEntity
import com.example.fitnesstracker.data.local.PaymentMethod
import com.example.fitnesstracker.domain.FinanceRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var financeRepository: FinanceRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val messages = try {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting SMS messages from intent", e)
            null
        }

        if (messages.isNullOrEmpty()) return

        val fullBody = StringBuilder()
        var timestamp = System.currentTimeMillis()
        var sender = ""

        for (sms in messages) {
            if (sms != null) {
                fullBody.append(sms.messageBody)
                if (sms.timestampMillis > 0) {
                    timestamp = sms.timestampMillis
                }
                if (sms.originatingAddress != null) {
                    sender = sms.originatingAddress ?: ""
                }
            }
        }

        val messageText = fullBody.toString().trim()
        if (messageText.isBlank()) return

        val suggestion = SmsExpenseParser.parse(messageText) ?: return

        Log.d(TAG, "New debit SMS detected: amount=${suggestion.amount}, source=${suggestion.source}")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val description = if (suggestion.source != "Bank SMS" && suggestion.source != "Bank transaction") {
                    "${suggestion.source} (SMS detected)"
                } else {
                    "Bank SMS detected"
                }

                val expense = ExpenseEntity(
                    amount = suggestion.amount,
                    category = ExpenseCategory.OTHER,
                    expenseDate = if (timestamp > 0) timestamp else System.currentTimeMillis(),
                    source = suggestion.source,
                    description = description,
                    paymentMethod = PaymentMethod.UPI,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val inserted = financeRepository.insertExpenseIfNotDuplicate(expense)
                if (inserted) {
                    Log.d(TAG, "Expense automatically saved to Room DB: ${expense.amount}")
                } else {
                    Log.d(TAG, "Duplicate SMS expense skipped: ${expense.amount}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving SMS expense", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
