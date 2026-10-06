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