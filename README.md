# Open Share Location Plugin for Conversations

[![Google Play](https://developer.android.com/images/brand/en_generic_rgb_wo_45.png)](https://play.google.com/store/apps/details?id=com.samwhited.opensharelocationplugin)
[![Buy Me A Coffee](https://www.buymeacoffee.com/assets/img/custom_images/purple_img.png)](https://www.buymeacoffee.com/samwhited)
[![Donate with Liberapay](https://liberapay.com/assets/widgets/donate.svg)](https://liberapay.com/SamWhited/donate)

This is a location sharing plugin for the XMPP client
[Conversations][conversations]. It uses data from Open Street Maps and doesn't
require the Google Play Services to be installed.

## Building

Before building, first sign up for a [Thunderforest] account and get an API key.
Then, edit or create the file `~/.gradle/gradle.properties` and add the key:

    thunderforestAPIKey=abc

## Open Source Services used:

 - [OSMDroid][osmdroid]
 - [OpenStreetMap][osm]
 - [OpenMap][openmap]

[Thunderforest]: http://www.thunderforest.com
[conversations]: https://github.com/siacs/Conversations
[osmdroid]: https://github.com/osmdroid/osmdroid
[jellybean]: https://developer.android.com/about/versions/android-4.2.html
[osm]: https://www.openstreetmap.org/
[openmap]: https://openmap.lt/
