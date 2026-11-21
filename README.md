# Dune IMAX Alarm v2.1 — Chennai

Checks public cinema listing pages **approximately every 15 minutes** on Android's JobScheduler (not exact when backgrounded / Doze):

- **PVR Palazzo** at Nexus Vijaya Mall, Vadapalani
- **INOX LUXE / IMAX** at Phoenix Marketcity, Velachery

For **December 18, 19 and 20, 2026**, the app retrieves six date-specific cinema pages from District and shows a status for each cinema/date. The current site selector is public-facing and may block automated access, so status may read "source unavailable." 

**IMPORTANT:** A public showtime listing, IMAX label, or "Book Tickets" button is not reliable evidence that seats can actually be bought. The 15-minute public-page checker **does not independently verify checkout and never triggers the booking siren by itself**. The loud alarm is triggered only by a *positive, date-specific* ChatGPT notification mentioning confirmed Dune / Chennai / IMAX booking. That underlying ChatGPT monitor runs hourly, not every 15 minutes.

A full-screen urgent notification, the phone's default alarm ringtone at maximum ALARM volume, vibration, and the two cinema links are included. Phone-call service is not included.

Install: grant notification + notification-listener permissions, full-screen alert access (if offered), and disable battery optimisation. Use TEST LOUD ALARM to check. The build is a debug APK, compiled on GitHub Actions; real-phone behavior remains unverified.

Android scheduling can defer jobs: 15 minutes is a minimum requested period, not a guaranteed interval. Dune's release dates and third-party webpage structures are external data and can change.
