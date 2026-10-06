# FitnessTracker Presentation Screens

Drop these Kotlin files into `app/src/main/java/com/example/fitnesstracker/presentation/`.

Included:
- TrainerTrackerApp.kt
- DashboardScreen.kt
- ClientsScreen.kt
- IncomeScreen.kt
- ExpenseScreen.kt
- ReportsScreen.kt
- SettingsScreen.kt

The screens use Material 3 and Navigation Compose. Clients/Income/Expense currently use local Compose state so the UI is immediately usable; replace that state with your existing Room/ViewModel flows when wiring persistence.

Expected existing routes: HOME, CLIENTS, INCOME, EXPENSES, REPORTS, SETTINGS.
