package com.example.fitnesstracker.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val sessionCharge: Long,
    val preferredDays: String = "",
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class PaymentMethod {
    CASH,
    UPI,
    BANK_TRANSFER,
    OTHER
}

enum class ExpenseCategory {
    FUEL,
    PUBLIC_TRANSPORT,
    PARKING,
    FOOD,
    EQUIPMENT,
    PHONE,
    SUBSCRIPTION,
    MAINTENANCE,
    OTHER
}

@Entity(
    tableName = "income",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("clientId"),
        Index("sessionDate")
    ]
)
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long? = null,
    val clientName: String = "Walk-in Client",
    val amount: Long,
    val sessionDate: Long = System.currentTimeMillis(),
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    indices = [
        Index("expenseDate"),
        Index("category")
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Long,
    val category: ExpenseCategory,
    val expenseDate: Long = System.currentTimeMillis(),
    val source: String = "",
    val description: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)