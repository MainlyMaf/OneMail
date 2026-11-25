# OneMail

Минималистичное Android-приложение для одноразовой почты на базе [Mail.tm](https://mail.tm).

## Возможности

- Создание временного почтового ящика в один клик
- Копирование адреса в буфер обмена
- Автообновление входящих каждые 5 секунд и Pull-to-Refresh
- Просмотр письма в нижнем листе с возможностью выделять и копировать текст
- Удаление ящика вместе со всеми письмами

## Стек

Kotlin, Jetpack Compose (Material 3), Retrofit 2 + OkHttp + Gson, SharedPreferences.

## Сборка

APK собирается в GitHub Actions (`.github/workflows/build.yml`) командой `gradle assembleDebug`.
Готовый файл публикуется артефактом `OneMail-APK`.
