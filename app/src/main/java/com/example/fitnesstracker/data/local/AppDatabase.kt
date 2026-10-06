package com.example.fitnesstracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class AppConverters {

    @TypeConverter
    fun paymentToString(value: PaymentMethod): String {
        return value.name
    }

    @TypeConverter
    fun stringToPayment(value: String): PaymentMethod {
        return PaymentMethod.valueOf(value)
    }

    @TypeConverter
    fun expenseToString(value: ExpenseCategory): String {
        return value.name
    }

    @TypeConverter
    fun stringToExpense(value: String): ExpenseCategory {
        return ExpenseCategory.valueOf(value)
    }
}

@Database(
    entities = [
        ClientEntity::class,
        IncomeEntity::class,
        ExpenseEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clientDao(): ClientDao

    abstract fun incomeDao(): IncomeDao

    abstract fun expenseDao(): ExpenseDao
}