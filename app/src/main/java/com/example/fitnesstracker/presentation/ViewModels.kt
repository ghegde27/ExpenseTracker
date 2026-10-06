package com.example.fitnesstracker.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.local.ClientEntity
import com.example.fitnesstracker.data.local.ExpenseCategory
import com.example.fitnesstracker.data.local.ExpenseEntity
import com.example.fitnesstracker.data.local.IncomeEntity
import com.example.fitnesstracker.data.local.PaymentMethod
import com.example.fitnesstracker.domain.ClientRepository
import com.example.fitnesstracker.domain.FinanceRepository
import com.example.fitnesstracker.domain.todayRange
import com.example.fitnesstracker.logging.AppLogger
import com.example.fitnesstracker.sms.SmsScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// Dashboard ViewModel
// ============================================================

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val financeRepo: FinanceRepository,
    private val clientRepo: ClientRepository,
    private val logger: AppLogger
) : ViewModel() {

    private val range
        get() = todayRange()

    val income: StateFlow<Long> =
        financeRepo.incomeBetween(range.first, range.last)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0L
            )

    val expenses: StateFlow<Long> =
        financeRepo.expenseBetween(range.first, range.last)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0L
            )

    val sessions: StateFlow<Int> =
        financeRepo.sessionsBetween(range.first, range.last)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0
            )

    val recentIncome: StateFlow<List<IncomeEntity>> =
        financeRepo.allIncome()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val clients: StateFlow<List<ClientEntity>> =
        clientRepo.clients()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        logger.i("DashboardVM", "DashboardViewModel initialized")
    }
}

// ============================================================
// Client ViewModel
// ============================================================

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val repo: ClientRepository,
    private val logger: AppLogger
) : ViewModel() {

    val clients: StateFlow<List<ClientEntity>> =
        repo.clients()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val clientSearchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val clientSearchResults: StateFlow<List<ClientEntity>> =
        clientSearchQuery
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    flowOf(emptyList())
                } else {
                    repo.search(query.trim())
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun searchClients(query: String) {
        clientSearchQuery.value = query
    }

    fun add(
        name: String,
        phone: String,
        charge: Long,
        preferredDays: String = "",
        notes: String = ""
    ) {
        logger.i("ClientVM", "Adding client: $name")
        viewModelScope.launch {
            runCatching {
                repo.add(
                    ClientEntity(
                        name = name,
                        phoneNumber = phone,
                        sessionCharge = charge,
                        preferredDays = preferredDays,
                        notes = notes
                    )
                )
            }.onFailure { t ->
                logger.e("ClientVM", "Failed to add client", t)
            }
        }
    }

    fun archive(id: Long) {
        logger.i("ClientVM", "Archiving client id: $id")
        viewModelScope.launch {
            runCatching {
                repo.archive(id)
            }.onFailure { t ->
                logger.e("ClientVM", "Failed to archive client", t)
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            runCatching {
                repo.delete(id)
            }
        }
    }
}

// ============================================================
// Income ViewModel
// ============================================================

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val clientsRepo: ClientRepository,
    private val financeRepo: FinanceRepository,
    private val logger: AppLogger
) : ViewModel() {

    val incomeList: StateFlow<List<IncomeEntity>> =
        financeRepo.allIncome()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val totalIncome: StateFlow<Long> =
        financeRepo.totalIncome()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0L
            )

    val clients: StateFlow<List<ClientEntity>> =
        clientsRepo.clients()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val clientSearchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val clientSearchResults: StateFlow<List<ClientEntity>> =
        clientSearchQuery
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    flowOf(emptyList())
                } else {
                    clientsRepo.search(query.trim())
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun searchClients(query: String) {
        clientSearchQuery.value = query
    }

    fun add(
        clientId: Long?,
        clientName: String,
        amount: Long,
        method: PaymentMethod,
        notes: String
    ) {
        logger.i("IncomeVM", "Adding income: $amount for $clientName")
        viewModelScope.launch {
            runCatching {
                financeRepo.addIncome(
                    IncomeEntity(
                        clientId = clientId,
                        clientName = clientName.ifBlank { "Walk-in Client" },
                        amount = amount,
                        sessionDate = System.currentTimeMillis(),
                        paymentMethod = method,
                        notes = notes
                    )
                )
            }.onFailure { t ->
                logger.e("IncomeVM", "Failed to add income", t)
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            runCatching {
                financeRepo.deleteIncome(id)
            }
        }
    }
}

// ============================================================
// Expense ViewModel
// ============================================================

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val financeRepo: FinanceRepository,
    private val smsScanner: SmsScanner,
    private val logger: AppLogger
) : ViewModel() {

    val expenseList: StateFlow<List<ExpenseEntity>> =
        financeRepo.allExpenses()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val totalExpenses: StateFlow<Long> =
        financeRepo.totalExpenses()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0L
            )

    private val _isScanningSms = MutableStateFlow(false)
    val isScanningSms: StateFlow<Boolean> = _isScanningSms.asStateFlow()

    private val _scanResultMessage = MutableStateFlow<String?>(null)
    val scanResultMessage: StateFlow<String?> = _scanResultMessage.asStateFlow()

    fun scanExistingSms(context: Context) {
        if (_isScanningSms.value) return
        logger.i("ExpenseVM", "Triggering historical SMS inbox scan")
        viewModelScope.launch {
            _isScanningSms.value = true
            _scanResultMessage.value = null
            try {
                val result = smsScanner.scanInbox(context)
                _scanResultMessage.value = when {
                    result.errorMessage != null -> result.errorMessage
                    result.addedCount > 0 -> "Found ${result.addedCount} new expenses from bank SMS!"
                    else -> "No new bank SMS expenses detected in inbox."
                }
            } catch (e: Exception) {
                logger.e("ExpenseVM", "Failed to scan SMS inbox", e)
                _scanResultMessage.value = "Error scanning SMS: ${e.localizedMessage}"
            } finally {
                _isScanningSms.value = false
            }
        }
    }

    fun clearScanMessage() {
        _scanResultMessage.value = null
    }

    fun add(
        amount: Long,
        category: ExpenseCategory,
        description: String,
        source: String = "Manual"
    ) {
        logger.i("ExpenseVM", "Adding expense: $amount ($category)")
        viewModelScope.launch {
            runCatching {
                financeRepo.addExpense(
                    ExpenseEntity(
                        amount = amount,
                        category = category,
                        description = description,
                        source = source,
                        expenseDate = System.currentTimeMillis()
                    )
                )
            }.onFailure { t ->
                logger.e("ExpenseVM", "Failed to add expense", t)
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            runCatching {
                financeRepo.deleteExpense(id)
            }
        }
    }
}