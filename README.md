# Dune + Avengers Cinema Alert v2.2 (Chennai)

**Dune: Part Three** remains high priority: IMAX at Palazzo (Nexus Vijaya Mall) and Phoenix Marketcity for **18–20 December 2026**. The phone's loud alarm and vibration only trigger for positive, date-specific, verified booking alerts received from the existing ChatGPT ticket monitor. Dune's public listing checker never claims checkout is working.

**Avengers: Doomsday** is lower priority: check **21–27 December 2026** for **MS Infinity Vision 3D** (not IMAX) in **any Chennai cinema**, from date-specific District movie listings. When a new matching showlisting appears and is not marked unavailable, issue **one regular Android notification per date** with a tap-through BookMyShow link. Do not ring the loud siren or invoke a full-screen alarm for Avengers.

### Accuracy & timing
- A public listing is not verified seat availability: **Avengers notifications explicitly say checkout is unverified**.
- District or BookMyShow may block automated requests, change HTML or redirect unsupported dates. The app does not bypass their protections; inaccessible results are retried.
- Android periodic jobs have a *minimum requested* period of 15 minutes; actual timing may be delayed due to Doze/battery optimisations. Not a guaranteed 15-minute alarm.
- Dune loud alarm is still dependent on the separate ChatGPT monitor's **hourly** notifications.
- Requires Android notifications and notification access for Dune. Alarm volume can be adjusted up to device maximum, subject to Android restrictions.
- Avengers has an independent on/off switch; Dune's settings are unaffected.
- Avengers checks stop after 27 December 2026.

### Build/install
Gradle 8.11.1, AGP 8.9.2, compileSdk 35. Debug APK built using GitHub Actions.
If replacing earlier app from a different signing key, uninstall old app (preferences will reset) and install the new one. Grant notifications, notification listener and battery exemptions. Test manually.
Compiled APK has not been tested on a physical device.
