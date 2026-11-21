# Chennai Cinema Alarm v2.4

## Dune high priority (Dec 18–20)
PVR Palazzo / Nexus Vijaya, INOX LUXE / Phoenix. The app discovers BookMyShow direct seat-layout links and checks them for **explicit available individual seat IDs** or a machine-readable positive available-seat count **on the correct Dune IMAX / venue / day page**. A listed time, generic "Available" legend, and "Please wait while we load the seats" do NOT count. If positive evidence is detected, the app triggers the system alarm ringtone and urgent notification, once per date, and saves the direct booking link. You can also use the in-app **live BookMyShow browser** to inspect rendered selectable seats with JavaScript (requires the app to be open and navigating to a seat map).

**No guarantee:** BookMyShow/District have no verified publicly supported seat-inventory API for this purpose. The checker may fail to discover direct show links, or pages may hide seat data behind JS/cookies/anti-bot systems, even when purchasing is possible. It will report UNVERIFIED and not ring rather than produce a false alarm. Android may defer 15-minute jobs; background alarm audio may be limited by OS restrictions. The browser's seat-element heuristic has not been tested against a real Chennai Dune checkout.

## Avengers normal priority (Dec 21–27)
MS Infinity Vision 3D in Chennai, ordinary listing notifications only, no siren.

## Permissions & install
No notification listener or accessibility permission is requested. Need standard notifications, alarm audio, background data. Check the APK with Play Protect enabled. Signed debug build; may conflict with an earlier install if signing keys differ. Manual TEST LOUD ALARM button included.
