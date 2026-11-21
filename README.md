# Chennai Ticket Watch v2.3 — reduced-permission build
This build addresses the Google Play Protect warning seen while sideloading v2.2 by **completely removing the ChatGPT notification-listener service**. It does not ask to read other applications' notifications, SMS, or accessibility content.

## Functionality
- Dune: public IMAX show listing checker for PVR Palazzo / Nexus Vijaya and INOX LUXE / Phoenix on Dec 18, 19 and 20, 2026.
- Avengers: *MS Infinity Vision 3D* normal-priority listing notifications for Dec 21–27, 2026; no siren.
- Both use Android's 15-minute periodic JobScheduler, which can be delayed by battery saving and network conditions.
- Test loud alarm: the built-in siren test uses the default system alarm sound and vibration. This is a **manual test**.
- Does not independently verify checkout, payment or tickets. Does not turn ChatGPT alerts into loud alarms. A separate trusted push service would be required to deliver verifiable automatic booking alarms without broad notification-reading access.
- The app does not guarantee that Google Play Protect will accept an unknown sideloaded APK; do not disable Play Protect if it blocks installation.

## Install
Remove a previously installed build if Android reports an APK signing conflict, then install. Grant normal notification permission. Do not enable restricted notification-reading permissions.
Build: AGP 8.9.2; Java 17; targetSdk 35; versionCode 5; versionName 2.3.
Real-device testing has not been performed.
