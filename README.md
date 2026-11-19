# Pay Date v2 (Simple Calendar)

An elegant, lightweight, open-source calendar and income tracker app for Android. No Google services, no accounts, no cloud sync — everything stays locally on your device in SQLite.

## What's New in v2.0

- **Dual-Purpose Engine:** General event tracking (meetings, personal, urgent, health) alongside granular income/pay date tracking.
- **Interactive Color Wheel:** Double-tap the month header to open the color wheel and dynamically change app accents on the fly.
- **Pay Date FAB:** Sleek Floating Action Button to quickly add earnings.
- **Secret Settings Panel:** Long-press the FAB to access custom rate multipliers (Overtime, Holiday, Double Pay) and include/exclude toggles.
- **Overtime Stepper:** Convenient `−` and `+` stepper buttons for overtime hours with automatic calculation.
- **Dark & Light Mode:** 1-tap theme toggle in the header.
- **Clean UI:** Clutter-free design with confirmed deletions and direct long-press day clear.

## Building

### Prerequisites

- Android SDK / build tools (`aapt2`, `d8`, `apksigner`, `javac`, `jar`)

```bash
bash build-apk.sh
```

## License

MIT License — see [LICENSE](LICENSE) for details.
