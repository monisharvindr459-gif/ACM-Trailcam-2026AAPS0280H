# Chennai ticket verifier v2.5 (Dune + Avengers)

- Dune IMAX, Chennai PVR Palazzo / INOX LUXE Phoenix, December 18/19/20: attempts to find direct BookMyShow seat-layout show URLs, checks for explicit positively available individual seat ID or machine-readable available-seat count, and **sounds the phone's built-in system alarm** only if that strict evidence exists. A live in-app WebView browser can detect enabled seat widgets on rendered seat maps while the app is open. Does not auto-purchase seats.
- Avengers: Doomsday, Chennai MS Infinity Vision 3D, December 21–27: same stricter seat-level evidence check using format-specific BookMyShow show pages, but sends ordinary notification only, once per day. It does NOT ring.
- *Checkout is not fully verified:* actual payment step is not attempted. Seat availability may change between check and purchase.
- **No guarantee of detecting availability:** booking sites use client-side requests and may block scraping; if links/seat data unavailable, app reports UNVERIFIED, triggers no false alarm. The checker is best effort, not a reliable 15-minute checkout monitor. Android JobScheduler can defer work.
- Remains devoid of notification listener or accessibility permissions. Google Play Protect may still block unknown sideloaded builds. Built via GitHub Actions; not tested on a physical Android device.
