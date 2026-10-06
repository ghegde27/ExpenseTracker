# Technical Design Document: Fitness Trainer Tracker

**Project Name**: Fitness Trainer Tracker (`FitnessTrainerTracker`)  
**Package Name**: `com.example.fitnesstracker`  
**Target Platform**: Android 8.0+ (API Level 26+)  
**Architecture**: MVVM + Clean Architecture + Unidirectional Data Flow (UDF)  
**Primary Stack**: Kotlin, Jetpack Compose, Material 3, Room DB, Hilt, Kotlin Coroutines & Flow  

---

## 1. Executive Summary

**Fitness Trainer Tracker** is an offline-first, modern Android mobile application tailored for independent fitness trainers, personal coaches, and gym instructors. The application empowers trainers to manage client rosters, log training session incomes, track business and travel expenses, and monitor financial health (net profit, revenue trends, expense category breakdowns) in real time.

---

## 2. High-Level System Architecture

The project follows Google's recommended **Android Application Architecture**, strictly adhering to **Clean Architecture** principles and **MVVM (Model-View-ViewModel)** with **Unidirectional Data Flow (UDF)**.

```
+-----------------------------------------------------------------------+
|                            PRESENTATION LAYER                         |
|  [Jetpack Compose UI] <---> [StateFlow / UIState] <---> [ViewModels]  |
+-----------------------------------------------------------------------+
                                  |
                                  v
+-----------------------------------------------------------------------+
|                               DOMAIN LAYER                            |
|                 [Repositories] <---> [Domain Models & Utils]          |
+-----------------------------------------------------------------------+
                                  |
                                  v
+-----------------------------------------------------------------------+
|                                DATA LAYER                             |
|          [Room DAOs] <---> [SQLite AppDatabase] <---> [Entities]       |
+-----------------------------------------------------------------------+
```

### Layer Responsibilities
1. **Presentation Layer (`com.example.fitnesstracker.presentation`)**:
   - Built entirely in **Jetpack Compose** with **Material 3** design guidelines.
   - Declarative Composables observe reactive `StateFlow` state from ViewModels.
   - Zero hardcoded fallback lists; all UI states reactively update from Room DB via Flow collectors.
2. **Domain Layer (`com.example.fitnesstracker.domain`)**:
   - Contains business logic, range computations (e.g. `todayRange()`), and repository interfaces.
3. **Data Layer (`com.example.fitnesstracker.data.local`)**:
   - **Room Database** (`AppDatabase`) serves as the Single Source of Truth (SSOT).
   - DAOs (`ClientDao`, `IncomeDao`, `ExpenseDao`) execute asynchronous queries returning Kotlin `Flow`.

---

## 3. Tech Stack & Dependencies

| Category | Technology / Library | Purpose |
| :--- | :--- | :--- |
| **Language** | Kotlin `1.9.x` | Modern, concise, type-safe programming language |
| **UI Framework** | Jetpack Compose + Material 3 | Declarative UI toolkit with dynamic color palettes |
| **Icons** | Material Icons Extended | Rich vector icons (`Icons.Default`, `Icons.AutoMirrored`) |
| **Dependency Injection** | Hilt (Dagger) `2.52` | Compile-time dependency injection |
| **Database** | Room `2.6.1` | SQLite object mapping abstraction layer |
| **Async & Streams** | Coroutines & Flow | Asynchronous programming and reactive streams |
| **Navigation** | Navigation Compose `2.8.5` | Type-safe single-activity composable navigation |
| **Lifecycle** | Lifecycle ViewModel Compose `2.8.7` | UI-aware state management and scope preservation |

---

## 4. Data Layer Architecture (Database Schema)

### Entity Relationship Diagram (ERD) Overview
```
+-------------------+        +-------------------+        +-------------------+
|   ClientEntity    |        |   IncomeEntity    |        |   ExpenseEntity   |
+-------------------+        +-------------------+        +-------------------+
| PK id (Long)      |<-------| PK id (Long)      |        | PK id (Long)      |
| name (String)     | 1    * | FK clientId (Long?|        | amount (Long)     |
| phoneNumber      |        | clientName (String|        | category (Enum)   |
| sessionCharge     |        | amount (Long)     |        | expenseDate (Long)|
| preferredDays     |        | sessionDate (Long)|        | source (String)   |
| isActive (Boolean)|        | paymentMethod     |        | description       |
+-------------------+        +-------------------+        +-------------------+
```

### Table Specifications

#### 1. `clients` Table
- **Primary Key**: `id` (`Long`, Auto-generated)
- **Columns**: `name`, `phoneNumber`, `email`, `address`, `sessionCharge`, `preferredDays`, `notes`, `isActive`, `createdAt`, `updatedAt`
- **Indexing**: Ordered by `name` with `isActive = 1`.

#### 2. `income` Table
- **Primary Key**: `id` (`Long`, Auto-generated)
- **Foreign Key**: `clientId` references `clients(id)` (`ON DELETE SET_NULL`)
- **Columns**: `clientId`, `clientName`, `amount`, `sessionDate`, `paymentMethod` (`CASH`, `UPI`, `BANK_TRANSFER`, `OTHER`), `notes`, `createdAt`
- **Indices**: `clientId`, `sessionDate`.

#### 3. `expenses` Table
- **Primary Key**: `id` (`Long`, Auto-generated)
- **Columns**: `amount`, `category` (`FUEL`, `PUBLIC_TRANSPORT`, `PARKING`, `FOOD`, `EQUIPMENT`, `PHONE`, `SUBSCRIPTION`, `MAINTENANCE`, `OTHER`), `expenseDate`, `source`, `description`, `paymentMethod`, `createdAt`, `updatedAt`
- **Indices**: `expenseDate`, `category`.

---

## 5. Screen Breakdown & UI Design

### 1. Dashboard Screen (`DashboardScreen.kt`)
- **Hero Banner**: Gradient background displaying current date, active clients count badge, and real-time Net Cash Flow calculation (`Income - Expenses`).
- **Financial Triad Cards**:
  - **Income Today**: Green Emerald container (`₹`).
  - **Expenses Today**: Sunset Coral container (`-₹`).
  - **Net Profit**: Royal Violet container (`AccountBalanceWallet`).
  - **Sessions Completed**: Amber Gold container (`FitnessCenter`).
- **Profit Ratio Gauge**: Linear progress bar measuring income vs expense percentage ratio.
- **Quick Action Carousel**: Tap buttons to navigate directly to **Add Income**, **Record Expense**, **Add Client**, or **Log Session**.
- **Active Client Schedule**: Displays active client roster for today's sessions with quick "+ Log Fee" triggers.

### 2. Clients Roster Screen (`ClientsScreen.kt`)
- **Header Banner**: Teal-Cyan gradient banner displaying total client count.
- **Search & Filter Bar**: Real-time search (`OutlinedTextField`) matching name or phone, paired with **All / Active / Archived** filter chips.
- **Client Cards**:
  - Auto-generated avatar gradient initials (`JD`, `AR`, `MM`).
  - Session charge badge pill (e.g. `₹800 / session`) and preferred days pill.
  - One-tap **Call** and **Message** shortcut buttons.
  - Expandable detail view revealing fitness goals/notes and an **Archive / Delete** action.
- **Add Client Dialog**: Input fields with quick session charge preset chips (`₹500`, `₹800`, `₹1000`, `₹1500`).

### 3. Income Logging Screen (`IncomeScreen.kt`)
- **Total Revenue Hero**: Sky Blue / Mint gradient card displaying total income and payment method breakdown (UPI, Cash, Bank).
- **Payment Method Filters**: Filter chip selection (**All**, **UPI**, **CASH**, **BANK_TRANSFER**).
- **Transaction List**: Color-coded payment method badges (Purple for UPI, Green for Cash, Blue for Bank Transfer).
- **Add Income Dialog**: Real-time auto-complete client search suggestions and quick amount preset buttons (`+₹500`, `+₹1000`, `+₹2000`, `+₹5000`).

### 4. Expense Tracker Screen (`ExpenseScreen.kt`)
- **Total Expense Hero**: Sunset Coral gradient card showing total business expenditures.
- **Category Filter Chips**: Filter expenses by category (**Equipment**, **Fuel**, **Food**, **Other**).
- **Add Expense Dialog**: Input fields with category choice chips and quick amount presets.

### 5. Analytics & Reports Screen (`ReportsScreen.kt`)
- **Hero Performance Overview**: Deep Violet gradient card displaying Total Revenue, Total Expenses, Net Savings, and Profit Margin %.
- **Period Filters**: Filter chip options (**All Time**, **This Month**, **This Week**).
- **Category Expense Breakdown**: Animated progress bars showing proportion of expenditure spent per category with distinct color branding.
- **Export Actions**: Export PDF and Share Report triggers.

### 6. Settings Screen (`SettingsScreen.kt`)
- **Trainer Profile Card**: Indigo card with trainer name, email, specialization title, and **Edit Profile** dialog.
- **Preferences**: Preferred currency switcher (`₹ INR`, `$ USD`, `€ EUR`, `£ GBP`).
- **Reminders & Automation**: Custom switches for session alerts, daily financial digests, and auto expense SMS parsing.
- **Data Management**: Category management, backup export, and database security settings.

---

## 6. Navigation Architecture

Navigation is managed via **Jetpack Navigation Compose** inside `TrainerTrackerApp.kt`:

```kotlin
NavHost(navController = nav, startDestination = Routes.HOME) {
    composable(Routes.HOME)     -> DashboardScreen
    composable(Routes.CLIENTS)  -> ClientsScreen
    composable(Routes.INCOME)   -> IncomeScreen
    composable(Routes.EXPENSES) -> ExpenseScreen
    composable(Routes.REPORTS)  -> ReportsScreen
    composable(Routes.SETTINGS) -> SettingsScreen
}
```

- **Bottom Navigation Bar**: Highlights active tab dynamically using `currentBackStackEntryAsState()`.
- **State Preservation**: Tab switches use `popUpTo(Routes.HOME) { saveState = true }`, `launchSingleTop = true`, and `restoreState = true` for seamless backstack management.

---

## 7. State Management & Threading Strategy

- **Single Source of Truth**: Room Database drives all UI state via `Flow`.
- **ViewModel State Conversion**: Room flows are converted into `StateFlow` via `.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = ...)` to survive configuration changes smoothly.
- **Background Dispatchers**: All Room mutations (`insert`, `update`, `archive`, `delete`) execute asynchronously on `Dispatchers.IO` via `viewModelScope.launch`.

---

## 8. Security & Best Practices

1. **Memory & Buffer Safety**: Managed through IDE-native file replacements and type-safe Kotlin Room entities.
2. **Foreign Key Protection**: `IncomeEntity` references `ClientEntity` with `ON DELETE SET_NULL` to maintain financial audit integrity even if client profiles are removed.
3. **Optimized Recomposition**: Jetpack Compose `key` parameters (`items(displayClients, key = { it.id })`) ensure efficient list rendering without unnecessary UI re-draws.

---

## 9. Conclusion & Roadmap

The **Fitness Trainer Tracker** architecture provides a robust, scalable foundation built with modern Android practices.

**Future Roadmap Extensions**:
- Integration of an automated SMS expense parser (`SmsExpenseParser.kt`) using Android SMS Receiver APIs.
- Voice command entry processing (`SpeechRecognizerManager.kt`).
- PDF report generation and cloud backup integration.
