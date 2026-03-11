# Health Compass Android

Новый Android-проект с функционалом, близким к текущему `healthbot`, но без Telegram:

- локальный учет сахара и давления;
- расписание ежедневных напоминаний;
- настройка режима измерений и часового пояса;
- журнал записей;
- экран отчетов с графиками;
- экспорт PDF-сводки.

## Стек

- Kotlin
- Jetpack Compose + Material 3
- Room
- WorkManager

## Структура

- `app/src/main/java/com/antonkiselev/healthcompanion/data` — база и репозиторий
- `app/src/main/java/com/antonkiselev/healthcompanion/notifications` — планирование напоминаний
- `app/src/main/java/com/antonkiselev/healthcompanion/reports` — экспорт PDF
- `app/src/main/java/com/antonkiselev/healthcompanion/ui` — ViewModel и Compose UI

## Запуск

1. Откройте папку `healthbot_android` в Android Studio.
2. Дождитесь Gradle Sync.
3. Запустите приложение на эмуляторе или устройстве с Android 8.0+.

## Сборка из терминала

```bash
./gradlew assembleDebug
```

APK после успешной сборки лежит в:

`app/build/outputs/apk/debug/app-debug.apk`

