# Simple Calendar / Pay Date

A lightweight, open-source calendar and income tracker app for Android. No Google services, no accounts, no cloud sync — everything stays on your device.

## Features

- **Calendar view** — month, week, and day views
- **Income tracking** — record daily income with Regular, Overtime, Holiday, Double Pay, and Extra categories
- **Month totals** — see your monthly income at a glance
- **Date selection** — tap any date to view or add income for that day
- **Long-press delete** — remove income from any date
- **Reminders** — optional reminders for your events
- **Dark mode** — toggleable dark theme
- **Custom accent color** — pick your own app color
- **Customizable formulas** — adjust overtime, holiday, and double-pay multipliers
- **Local storage** — all data stored locally in SQLite; no cloud, no accounts

## No Google Services

This app does not use Google Play Services, Firebase, Google Sign-In, or any Google APIs. It works entirely offline.

## Building

### Prerequisites

- Android SDK (API 21+)
- Java 8+
- aapt2, d8, apksigner

### Build

```bash
sh build-apk.sh
```

This produces `build/com.example.simplecalendar.apk`.

## License

MIT License — see [LICENSE](LICENSE) for details.

## Screenshots

Add screenshots here.

---

Made with ❤️ for offline-first, privacy-respecting Android apps.
