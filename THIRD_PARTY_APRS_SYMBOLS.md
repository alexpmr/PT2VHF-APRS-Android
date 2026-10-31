# APRS icon artwork / credits

The Android application uses the APRS primary and alternate symbol sheets from
[hessu/aprs-symbols](https://github.com/hessu/aprs-symbols), compiled by
Heikki Hannikainen (OH7LZB) for the aprs.fi amateur-radio community.
The atlas files are fetched at build time from pinned commit
`f2286a9cd43eb6ba4501250b4c39fff111e3796c` with exact Git object hashes
by `scripts/fetch-aprs-symbols.sh` and embedded locally in the APK.

The upstream artwork comprises individually credited and differently licensed
designs; attribution, rights, and trademark notices are documented per symbol
in the upstream [COPYRIGHT.md](https://github.com/hessu/aprs-symbols/blob/master/COPYRIGHT.md).
Do not infer that all individual graphical symbols share one license.

Primary/secondary symbol indexing follows APRS symbol codes 33..126 mapped
left-to-right across 16 columns, with overlay characters drawn on applicable
alternate table symbols. These images are assets, not transmitted over APRS RF.
