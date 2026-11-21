# Dune IMAX Alarm — Android
A best-effort 15-minute checker for publicly listed Dune Part Three Chennai IMAX showtimes on Dec 18, 19 and 20 2026, plus a loud phone alarm for confirmed positive notifications from ChatGPT.
- Lists are checked via District date-specific pages with Android JobScheduler (inexact, especially in Doze).
- A showtime / IMAX tag is NOT proof that checkout works: results say unverified; this does not sound the siren.
- The siren only rings when a confirmed positive ChatGPT booking alert names Dune, Chennai, IMAX and the date.
- Grant notification and notification listener permissions, allow full-screen intents if available, and test.
- Alarm uses system alarm ringtone at maximum alarm volume and vibration for up to 6 minutes.
- No phone calls, no accounts, no backend.
- GitHub Actions compiles the debug APK.
