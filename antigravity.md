# Android Life Time Tracker — Build Specification

You are the lead Android engineer for this repository.

Build a native Android app called **Life Time Tracker**. It helps a user understand where their time goes by combining:

1. Manual activity logging
2. Voice-based activity entry
3. Device app-usage tracking
4. Daily category limits and notifications
5. Daily and weekly analytics

Do not build every feature at once. Follow the phased MVP plan below.

---

## Working Rules

- Use Kotlin only.
- Use Jetpack Compose and Material 3 only; do not use XML layouts.
- Use MVVM with a practical clean architecture structure:
  - `data`: Room, DataStore, Android system integrations, repositories
  - `domain`: models, repository interfaces, use cases
  - `presentation`: Compose screens, ViewModels, navigation
- Use Hilt for dependency injection.
- Use Room for persistent structured data.
- Use DataStore Preferences for small app settings.
- Use Kotlin coroutines and Flow.
- Target current stable Android SDK versions compatible with Android Studio.
- Use Gradle Kotlin DSL (`build.gradle.kts`).
- Minimum SDK: 26.
- Add comments only where code is non-obvious.
- Do not hardcode category database IDs. Use stable category keys/slugs such as `work`, `learning`, `personal`, `social_video`, and `other`.
- Make the project compile after each phase.
- Add unit tests for parsers, ViewModels, and important use cases.
- Do not add an LLM API in the MVP. Voice parsing must work locally with simple rules.

Before changing code:
1. Inspect the current repository.
2. Show a short implementation plan.
3. Create the required directories and files.
4. Implement one phase at a time.
5. Run the Gradle build and tests.
6. Fix compilation errors before proceeding.

---

## Product Goal

The user should be able to answer:

- How much time did I spend today in each category?
- Did I exceed a daily limit?
- Which phone apps consumed my time?
- Can I quickly log an offline activity by typing or speaking?

Example voice input:

> "Studied English for 90 minutes"

Expected parsed result:

- Category: `learning`
- Duration: `90` minutes
- Source: `VOICE`
- Note: original recognized text

---

## MVP Scope

### Required MVP Features

1. Onboarding and permissions
2. Predefined editable categories
3. Manual time logging
4. Voice input with Android `SpeechRecognizer`
5. Rule-based voice text parser
6. Android Usage Access integration
7. Daily per-category usage aggregation
8. Daily limits with warning notifications
9. Today dashboard
10. Basic weekly analytics
11. Settings screen

### Explicitly Out of Scope for MVP

- User accounts
- Firebase backend
- Social comparison/rankings
- Cloud synchronization
- Paid subscriptions
- LLM API integration
- Complex calendar scheduling
- Automatic background microphone listening
- Exact per-second cross-device tracking

---

## Permissions and Android Constraints

The app must request or guide the user to grant these permissions/settings:

- `android.permission.RECORD_AUDIO` for voice logging
- `android.permission.POST_NOTIFICATIONS` on Android 13+
- Usage Access is granted through Android Settings using:
  `Settings.ACTION_USAGE_ACCESS_SETTINGS`

Important:

- Usage statistics are only available after the user manually grants Usage Access.
- Do not claim that app usage is real-time or perfectly exact.
- The dashboard must show a clear explanation when Usage Access is missing.
- The app must continue working fully for manual logs even if Usage Access is denied.
- Use WorkManager for periodic sync. Do not depend on exact timing.

---

## Tech Stack

Use:

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Hilt
- Room
- DataStore Preferences
- WorkManager
- Android UsageStatsManager
- SpeechRecognizer
- NotificationCompat
- Kotlin Coroutines and Flow
- JUnit for unit tests
- Compose UI tests where useful

Use a simple custom Compose chart for MVP. Do not add a large chart library unless needed.

---

## Project Structure

Create this structure:

```text
LifeTimeTracker/
├── ANTIGRAVITY.md
├── README.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/lifetimetracker/
│       │   │   ├── LifeTimeTrackerApplication.kt
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/
│       │   │   │   ├── local/
│       │   │   │   │   ├── AppDatabase.kt
│       │   │   │   │   ├── dao/
│       │   │   │   │   └── entity/
│       │   │   │   ├── preferences/
│       │   │   │   ├── repository/
│       │   │   │   └── system/
│       │   │   │       ├── UsageStatsDataSource.kt
│       │   │   │       ├── VoiceRecognizerHelper.kt
│       │   │   │       └── NotificationHelper.kt
│       │   │   ├── di/
│       │   │   │   ├── DatabaseModule.kt
│       │   │   │   ├── RepositoryModule.kt
│       │   │   │   └── SystemModule.kt
│       │   │   ├── domain/
│       │   │   │   ├── model/
│       │   │   │   ├── repository/
│       │   │   │   ├── usecase/
│       │   │   │   └── parser/
│       │   │   │       └── ActivityTextParser.kt
│       │   │   ├── presentation/
│       │   │   │   ├── navigation/
│       │   │   │   ├── screen/
│       │   │   │   │   ├── onboarding/
│       │   │   │   │   ├── dashboard/
│       │   │   │   │   ├── logactivity/
│       │   │   │   │   ├── analytics/
│       │   │   │   │   ├── categories/
│       │   │   │   │   └── settings/
│       │   │   │   ├── component/
│       │   │   │   ├── theme/
│       │   │   │   └── viewmodel/
│       │   │   └── worker/
│       │   │       └── UsageSyncWorker.kt
│       │   ├── test/
│       │   │   └── java/com/example/lifetimetracker/
│       │   │       ├── domain/parser/
│       │   │       └── presentation/
│       │   └── androidTest/
│       │       └── java/com/example/lifetimetracker/
│       └── ...
```

Use package name:

```text
com.example.lifetimetracker
```

Ask me before changing the package name.

---

## Data Model

### Category

Create a Room entity for categories.

Fields:

```kotlin
id: Long
key: String               // Unique stable key, e.g. "work"
name: String              // User-visible name
colorHex: String
iconName: String
dailyLimitMinutes: Int?   // null = no limit
isSystem: Boolean
sortOrder: Int
createdAt: Long
updatedAt: Long
```

Seed these system categories on first launch:

| Key | Name | Default daily limit |
|---|---|---:|
| `work` | Work | none |
| `learning` | Learning | none |
| `personal` | Personal | none |
| `social_video` | Social & Video | 60 minutes |
| `other` | Other | none |

System categories may be edited, but must not be deleted.

### Activity Log

Create a Room entity for manual and voice-created time entries.

Fields:

```kotlin
id: Long
categoryId: Long
startTime: Long
endTime: Long
durationMinutes: Int
source: ActivitySource
note: String?
date: String              // ISO local date: YYYY-MM-DD
createdAt: Long
updatedAt: Long
```

Create:

```kotlin
enum class ActivitySource {
    MANUAL,
    VOICE,
    AUTO_USAGE
}
```

### App Usage Aggregate

Do not store a separate raw log for every app event in the MVP.

Store one aggregate per day, package name, and category:

```kotlin
id: Long
date: String
packageName: String
appLabel: String?
categoryId: Long
foregroundMinutes: Int
lastSyncedAt: Long
```

Add a unique index for:

```text
(date, packageName)
```

This lets the background worker safely update an existing record instead of duplicating data.

### Settings

Use DataStore for:

```kotlin
onboardingCompleted: Boolean
notificationsEnabled: Boolean
usageAccessPromptDismissed: Boolean
voiceLanguageTag: String
```

Default voice language:

```text
en-US
```

Design the parser so Russian and Ukrainian language support can be added later.

---

## Repository Interfaces

Create repository interfaces in the `domain` layer and implementations in `data`.

Minimum repositories:

```kotlin
CategoryRepository
ActivityRepository
UsageRepository
SettingsRepository
```

Required operations:

- Observe categories
- Create, update, and delete user categories
- Observe logs by date range
- Add, update, and delete activity logs
- Calculate total minutes per category for a date range
- Sync device usage statistics
- Observe daily usage by category
- Read and update settings

---

## Voice Parser Requirements

Implement an offline, rule-based parser first.

Create:

```kotlin
data class ParsedActivity(
    val durationMinutes: Int,
    val categoryKey: String?,
    val confidence: ParseConfidence
)

enum class ParseConfidence {
    HIGH,
    LOW,
    FAILED
}
```

The parser should recognize common English phrases such as:

```text
worked for 2 hours
studied for 90 minutes
watched YouTube for 1 hour 20 minutes
spent 45 minutes on social media
```

Rules:

- Support hours, minutes, and combined duration.
- Convert decimal hours when reasonable, for example `1.5 hours` to `90 minutes`.
- Match category keywords against known category names and synonyms.
- If category is unknown but duration is known, return a low-confidence result and require category selection in UI.
- If duration is missing, show the recognized text and ask the user to enter a duration.
- Never save a voice result automatically without a visible confirmation step.

Include unit tests for the parser.

---

## App Usage Mapping

Create an editable app-category mapping strategy.

Initial built-in mappings can include:

```text
YouTube -> social_video
Instagram -> social_video
TikTok -> social_video
Facebook -> social_video
X / Twitter -> social_video
Slack -> work
Microsoft Teams -> work
Google Docs -> work
Duolingo -> learning
Coursera -> learning
Telegram -> personal
WhatsApp -> personal
```

Requirements:

- Unknown packages go to `other`.
- Keep mapping logic separate from Room entities.
- Design it so the user can later override an app’s category.
- Aggregate usage by category for the dashboard.

---

## Screens

### 1. Onboarding Screen

Show on first launch.

Content:

- App purpose
- Button: `Grant Usage Access`
- Button: `Allow Notifications`
- Button: `Continue`

Rules:

- Usage Access is optional but clearly recommended.
- The user can continue without granting it.
- Explain that manual logging remains available.

### 2. Dashboard Screen

This is the app start screen after onboarding.

Show:

- Today’s date
- Total tracked minutes today
- Category limit progress cards
- Manual/voice activity totals
- Automatic phone usage totals
- A prominent `Add Activity` button
- A visible warning if Usage Access has not been granted
- Last successful usage sync time

### 3. Add Activity Screen

Provide:

- Category dropdown or selectable chips
- Duration input in minutes
- Optional start time
- Notes field
- Save button
- Microphone button

Voice workflow:

1. User taps microphone.
2. Request `RECORD_AUDIO` permission if necessary.
3. Start `SpeechRecognizer`.
4. Display recognized text.
5. Parse text.
6. Prefill category and duration if recognized.
7. User reviews and taps Save.

### 4. Analytics Screen

MVP analytics:

- Date range: today, last 7 days, last 30 days
- Category totals
- Simple category pie/donut chart
- Daily bar chart for the selected range
- Separate totals for manual/voice and automatic usage

### 5. Categories Screen

Allow the user to:

- View categories
- Add a custom category
- Rename a category
- Change color
- Set or remove daily limit
- Reorder categories
- Delete custom categories only

### 6. Settings Screen

Include:

- Notifications enabled toggle
- Usage Access status and button to open system settings
- Speech recognition language selection
- Privacy explanation
- Clear local data action with confirmation dialog

---

## Notifications and Limits

Implement daily category limits.

Rules:

- When usage reaches 80% of a category’s daily limit, send one warning notification.
- When usage reaches 100%, send one limit-reached notification.
- Do not repeatedly notify for the same category and local date.
- Reset notification state automatically for a new day.
- Notifications must respect the user’s notification setting and Android notification permission.
- A category without a limit must never produce a limit notification.

Use a dedicated notification channel:

```text
daily_limits
```

---

## WorkManager Usage Sync

Create `UsageSyncWorker`.

Requirements:

- Run periodic background sync using WorkManager.
- Sync today’s UsageStats data.
- Avoid duplicate aggregate records by using Room upsert behavior.
- Gracefully handle missing Usage Access.
- Update the last-sync timestamp only after a successful sync.
- After sync, evaluate category limits and issue notifications if appropriate.
- Make worker behavior testable by placing business logic in use cases/repositories.

Also trigger a best-effort sync when the Dashboard becomes visible.

---

## Navigation

Use bottom navigation:

```text
Dashboard | Analytics | Categories | Settings
```

The Add Activity screen should open from a floating action button or primary dashboard button.

---

## Phased Implementation Plan

### Phase 0 — Bootstrap

Create the Android project and configure:

- Compose
- Material 3
- Hilt
- Room
- DataStore
- WorkManager
- Navigation
- Test dependencies

Create a minimal app that compiles and launches.

### Phase 1 — Local Data Foundation

Implement:

- Room database
- Entities
- DAOs
- Category seed data
- Repositories
- Hilt modules
- Unit tests for repositories where practical

Acceptance criteria:

- App launches.
- Five default categories appear.
- Database persists after restarting the app.

### Phase 2 — Manual Logging

Implement:

- Add Activity screen
- Manual log creation, editing, deletion
- Dashboard list of today’s logs
- Totals by category

Acceptance criteria:

- User can save an activity.
- Dashboard totals update immediately.
- Data remains after app restart.

### Phase 3 — Voice Logging

Implement:

- Audio permission flow
- SpeechRecognizer helper
- Rule-based `ActivityTextParser`
- Review-and-save flow
- Parser unit tests

Acceptance criteria:

- Recognized voice text appears in the form.
- A phrase such as “studied for 90 minutes” prefills Learning and 90 minutes.
- No entry is saved until the user confirms.

### Phase 4 — Usage Tracking

Implement:

- Usage Access onboarding
- UsageStats data source
- Package-to-category mapping
- Daily aggregate storage
- UsageSyncWorker
- Dashboard automatic-usage section

Acceptance criteria:

- App gracefully handles no Usage Access.
- With Usage Access, category usage aggregates appear on the dashboard.
- Repeated syncs do not duplicate totals.

### Phase 5 — Limits and Notifications

Implement:

- Category daily-limit editing
- Limit progress components
- 80% and 100% notifications
- Notification de-duplication per category/date

Acceptance criteria:

- A 60-minute social limit warns at 48 minutes.
- It sends only one warning and one limit-reached message per day.

### Phase 6 — Analytics and Polish

Implement:

- 7-day and 30-day summaries
- Pie/donut chart
- Daily bar chart
- Empty states, loading states, error states
- Accessibility labels
- README instructions

Acceptance criteria:

- User can understand category totals over 7 and 30 days.
- App has no known build errors.
- README explains setup, permissions, and limitations.

---

## README Requirements

Create a useful `README.md` containing:

- App overview
- Feature list
- Architecture overview
- Requirements
- Android Studio setup instructions
- How to build and run
- Required permissions
- How to grant Usage Access
- Privacy notes
- Known limitations
- Test instructions
- Screenshot placeholders

State clearly:

- The app stores data locally in the MVP.
- Usage tracking depends on Android Usage Access.
- Device usage data may not perfectly represent all activity.
- Speech recognition availability can vary by device and installed services.

---

## Quality Checklist

Before declaring the MVP complete:

- Run `./gradlew test`
- Run a debug build
- Fix all compilation errors
- Check Room migrations or destructive-migration policy
- Verify the app works without Usage Access
- Verify the app works with notifications denied
- Verify a manual log can be added and removed
- Verify parser tests pass
- Verify no duplicate automatic usage aggregate records are created
- Verify a daily limit notification is not repeated

---

## First Task

Start with Phase 0 and Phase 1 only.

First, inspect the repository and report what exists. Then create the Android project structure, Gradle configuration, Room database foundation, Hilt modules, category seed data, and a minimal Compose screen listing the seeded categories.

Do not begin Phase 2 until Phase 1 builds successfully and I approve moving forward.