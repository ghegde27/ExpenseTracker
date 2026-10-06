package com.example.fitnesstracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients WHERE isActive = 1 AND (name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%') ORDER BY name")
    fun search(query: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id")
    fun observe(id: Long): Flow<ClientEntity?>

    @Query("SELECT * FROM clients WHERE isActive = 1 ORDER BY name")
    fun observeAll(): Flow<List<ClientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: ClientEntity): Long

    @Update
    suspend fun update(client: ClientEntity)

    @Query("UPDATE clients SET isActive = 0, updatedAt = :now WHERE id = :id")
    suspend fun archive(id: Long, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface IncomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: IncomeEntity): Long

    @Query("SELECT * FROM income ORDER BY sessionDate DESC")
    fun observeAll(): Flow<List<IncomeEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM income")
    fun sumAll(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM income WHERE sessionDate >= :start AND sessionDate <= :end")
    fun sumBetween(start: Long, end: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM income WHERE sessionDate >= :start AND sessionDate <= :end")
    fun countBetween(start: Long, end: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM income WHERE clientId = :clientId")
    fun clientTotal(clientId: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM income WHERE clientId = :clientId")
    fun clientSessions(clientId: Long): Flow<Int>

    @Query("DELETE FROM income WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses")
    fun sumAll(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE expenseDate >= :start AND expenseDate <= :end")
    fun sumBetween(start: Long, end: Long): Flow<Long>

    @Query("SELECT * FROM expenses WHERE expenseDate >= :start AND expenseDate <= :end ORDER BY expenseDate DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE amount = :amount AND expenseDate >= :start AND expenseDate <= :end")
    suspend fun findInTimeRange(amount: Long, start: Long, end: Long): List<ExpenseEntity>

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun delete(id: Long)
}