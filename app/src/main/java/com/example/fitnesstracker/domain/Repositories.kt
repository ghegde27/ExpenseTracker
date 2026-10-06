package com.example.fitnesstracker.domain

import com.example.fitnesstracker.data.local.ClientDao
import com.example.fitnesstracker.data.local.ClientEntity
import com.example.fitnesstracker.data.local.ExpenseDao
import com.example.fitnesstracker.data.local.ExpenseEntity
import com.example.fitnesstracker.data.local.IncomeDao
import com.example.fitnesstracker.data.local.IncomeEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class ClientRepository @Inject constructor(
    private val dao: ClientDao
) {
    fun clients(): Flow<List<ClientEntity>> = dao.observeAll()
    fun search(query: String): Flow<List<ClientEntity>> = dao.search(query)
    fun observe(id: Long): Flow<ClientEntity?> = dao.observe(id)
    suspend fun add(client: ClientEntity): Long = dao.insert(client)
    suspend fun update(client: ClientEntity) = dao.update(client)
    suspend fun archive(id: Long) = dao.archive(id)
    suspend fun delete(id: Long) = dao.delete(id)
}

class FinanceRepository @Inject constructor(
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao
) {
    suspend fun addIncome(income: IncomeEntity): Long = incomeDao.insert(income)
    suspend fun addExpense(expense: ExpenseEntity): Long = expenseDao.insert(expense)

    suspend fun insertExpenseIfNotDuplicate(
        expense: ExpenseEntity,
        timeWindowMs: Long = 10 * 60 * 1000L
    ): Boolean {
        val start = expense.expenseDate - timeWindowMs
        val end = expense.expenseDate + timeWindowMs
        val candidates = expenseDao.findInTimeRange(expense.amount, start, end)

        val newSource = expense.source.trim().lowercase()
        val newDesc = expense.description.trim().lowercase()

        for (existing in candidates) {
            val existingSource = existing.source.trim().lowercase()
            val existingDesc = existing.description.trim().lowercase()

            val sourceMatches = newSource.isNotBlank() && existingSource.isNotBlank() &&
                    (newSource.contains(existingSource) || existingSource.contains(newSource))

            val descMatches = newDesc.isNotBlank() && existingDesc.isNotBlank() &&
                    (newDesc.contains(existingDesc) || existingDesc.contains(newDesc))

            val oneIsGenericBank = isGenericBankOrWallet(existing.source) || isGenericBankOrWallet(existing.description)
            val otherIsGenericBank = isGenericBankOrWallet(expense.source) || isGenericBankOrWallet(expense.description)

            if (sourceMatches || descMatches || oneIsGenericBank || otherIsGenericBank) {
                if (oneIsGenericBank && !otherIsGenericBank && expense.description.isNotBlank()) {
                    expenseDao.insert(
                        existing.copy(
                            description = expense.description,
                            source = expense.source.ifBlank { existing.source },
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
                return false
            }
        }

        expenseDao.insert(expense)
        return true
    }

    private fun isGenericBankOrWallet(str: String): Boolean {
        val lower = str.lowercase()
        return lower.contains("bank") || lower.contains("hdfc") || lower.contains("icici") ||
                lower == "bank transaction" || lower == "bank sms" || lower == "manual"
    }

    fun allIncome(): Flow<List<IncomeEntity>> = incomeDao.observeAll()
    fun totalIncome(): Flow<Long> = incomeDao.sumAll()
    fun incomeBetween(start: Long, end: Long): Flow<Long> = incomeDao.sumBetween(start, end)

    fun allExpenses(): Flow<List<ExpenseEntity>> = expenseDao.observeAll()
    fun totalExpenses(): Flow<Long> = expenseDao.sumAll()
    fun expenseBetween(start: Long, end: Long): Flow<Long> = expenseDao.sumBetween(start, end)
    fun expensesBetween(start: Long, end: Long): Flow<List<ExpenseEntity>> = expenseDao.observeBetween(start, end)

    fun sessionsBetween(start: Long, end: Long): Flow<Int> = incomeDao.countBetween(start, end)
    fun clientTotal(clientId: Long): Flow<Long> = incomeDao.clientTotal(clientId)
    fun clientSessions(clientId: Long): Flow<Int> = incomeDao.clientSessions(clientId)

    suspend fun deleteIncome(id: Long) = incomeDao.delete(id)
    suspend fun deleteExpense(id: Long) = expenseDao.delete(id)
}

fun todayRange(): LongRange {
    val zone = ZoneId.systemDefault()
    val start = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
    val end = LocalDate.now().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
    return start..end
}