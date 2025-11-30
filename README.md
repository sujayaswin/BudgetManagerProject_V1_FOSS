# Budget Manager - Android (Java) — APK-ready project

This is an APK-ready Android Studio project (Java) that targets Android API 16+ (minSdk 16) and is purely local.
Features include:
- Record income and expenses with date, category, amount, notes and type (INCOME / EXPENSE).
- Monthly summary on main page (total income, total expenses, balance) with Prev/Next navigation and month picker.
- Export transactions as CSV (visible in the device's Downloads folder so you can copy via USB or open in Files app) and import CSV from the Downloads folder.
- Local storage via SQLite (no network required).

To build and produce an APK:
1. Download and unzip the project.
2. Open the folder in Android Studio (File → Open).
3. Build → Make Project, then Build → Build Bundle(s) / APK(s) → Build APK(s).
4. Install the APK on your Android 16 device (USB or transfer).

CSV notes:
- Exported CSV files are written to the device **Downloads** directory.
- Import expects a file named `import.csv` in the device Downloads directory (you can rename/copy your file to that name).

