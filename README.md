# Fitness Trainer Tracker

Offline-first Android app starter for a fitness trainer to track clients, training income, expenses, dashboard trends, voice-assisted transactions, and opt-in SMS expense suggestions.

## Architecture
UI (Compose) -> ViewModel -> Repository -> Room / Android APIs

## Included
- Kotlin + Jetpack Compose + Material 3
- MVVM/Clean Architecture starter
- Hilt DI
- Room: Client, Income, Expense
- Client search while adding income
- Dashboard with income/expense/profit/session metrics
- Income and travel carousel UI starter
- Voice transaction parser + confirmation flow hook
- SMS expense parser with suggestion-only design
- Permission abstraction
- Structured logging wrapper
- Unit tests for voice/SMS parsing

## Open
Open this directory in Android Studio and let Gradle sync. Use JDK 17.

## Important Android platform note
SMS permissions and background SMS processing are restricted by Android/Google Play policies. The implementation intentionally treats SMS as optional and confirmation-only. Before publishing, verify the current Play policy and use the least-privileged supported API for the target distribution.

## Next production hardening
- Replace the manual voice text hook with SpeechRecognizer / on-device recognition where supported.
- Add runtime permission Compose flows.
- Add FusedLocationProvider travel session tracking with foreground-service policy compliance if needed.
- Add date-range queries and actual chart library/Compose Canvas charts.
- Add SMS BroadcastReceiver only if the distribution/policy permits it.
- Add Room migrations and encrypted backup strategy if cloud sync is introduced.
- Add comprehensive UI/instrumentation tests.
