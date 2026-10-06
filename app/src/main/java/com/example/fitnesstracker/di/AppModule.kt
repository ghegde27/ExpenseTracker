package com.example.fitnesstracker.di

import android.content.Context
import androidx.room.Room
import com.example.fitnesstracker.data.local.AppDatabase
import com.example.fitnesstracker.data.local.ClientDao
import com.example.fitnesstracker.data.local.IncomeDao
import com.example.fitnesstracker.data.local.ExpenseDao
import com.example.fitnesstracker.logging.AppLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun db(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "trainer_tracker.db")
        .fallbackToDestructiveMigration()
        .build()
    @Provides fun clients(db: AppDatabase): ClientDao = db.clientDao()
    @Provides fun income(db: AppDatabase): IncomeDao = db.incomeDao()
    @Provides fun expenses(db: AppDatabase): ExpenseDao = db.expenseDao()
    @Provides @Singleton fun logger(): AppLogger = AppLogger()
}
