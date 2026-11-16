# PT2VHF Speed Alert — v1.0.0-beta.1

Standalone Android app (Kotlin; GPS, no maps, no radar, no internet permission).
The display shows current GNSS speed and an offline OpenStreetMap speed limit when a segment can be identified. An explicit manual override can be selected for missing data.
Zero tolerance means > limit activates 250 ms repeating beeps separated by 350 ms silence, at 80% of the media audio route. Display stays active in background via location Foreground Service after the user starts it.

## Android build
Use Gradle 8.10.2, AGP 8.7.2, JDK 17 and SDK 35.
Offline database generator: Python 3, osmium, Centro-Oeste PBF from Geofabrik.
Run `gradle :app:assembleDebug` inside speed-alert.

## License and safety
Data © OpenStreetMap contributors, ODbL 1.0. OpenStreetMap may not have maxspeed or may be outdated; unclear roads show unknown rather than guessing. Traffic signs prevail. Manual limit is explicitly marked. Not field-tested; use on a fixed stand without interacting while driving.

The project is on a dedicated build-only branch of a pre-existing GitHub repository; main is unaffected.
