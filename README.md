# Dune + Avengers Chennai Seat Watch v2.7

## Changes from v2.6
- Dune urgent notification channel now has a system-managed ALARM sound (new channel ID) as a fallback if Android stops the app's own looping ringtone. The alarm notification has a STOP ALARM action.
- Dune seat verifier now requires explicit available individual-seat evidence; aggregate counts alone no longer qualify.
- Added a field for an **Avengers Infinity Vision 3D** direct BookMyShow seat-layout link to help verify dynamically generated show pages.
- Kept Play Protect sensitive notification-reader/accessibility permissions absent.
- Dune high priority: Chennai IMAX at PVR Palazzo / Nexus Vijaya and INOX LUXE / Phoenix on Dec 18, 19, 20.
- Avengers normal notification only: Chennai MS Infinity Vision 3D from Dec 21 to 27.
- Best effort Android JobScheduler 15-minute polling, not guaranteed exactly every 15 minutes.

## Critical limitations
- A detected available seat is not the same as completing a purchase. No payment, cart or account authentication is performed.
- Live site responses and Android background execution are untested on a physical phone; BookMyShow may hide seat data or block requests. The app reports UNVERIFIED and doesn't sound a false alarm in those cases.
- Full-screen alerts and alarm volume remain subject to Android/device controls. The app cannot place a real phone call.
- The APP will not ring until explicit valid Dune seat data is detected. The built-in TEST LOUD ALARM button can check sound on your device.
- Installing a debug signed APK may still trigger Play Protect. Do not disable Play Protect to install it.
