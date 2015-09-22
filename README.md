# Open Share Location Plugin for Conversations

[![Google Play](https://developer.android.com/images/brand/en_generic_rgb_wo_45.png)](https://play.google.com/store/apps/details?id=com.samwhited.opensharelocationplugin)
[![Amazon App Store](https://images-na.ssl-images-amazon.com/images/G/01/AmazonMobileApps/amazon-apps-store-us-black.png)](https://www.amazon.com/gp/product/B015M1CBJO)

This is a location sharing plugin for the XMPP client
[Conversations][conversations]. Unlike the
[official plugin][conversations-loc], this one uses data from Open Street Maps
and doesn't require the Google Play Services to be installed. Consequentially,
it will probably not be as accurate as the official one (which uses the Google
API's), or will eat a bit more battery as it only uses basic AOSP geolocation
services (albeit with a few optimizations to utilize multiple providers and
save battery where possible).

## Open Source Services used:

 - [OSMDroid][osmdroid]
 - [Mapnik][mapnik]

## Requirements

 - Conversations ≥1.2.0


## Donate

If you'd like to donate to this project, you can send bitcoin to:
`1PYd7Koqd3ucSxKQRZQZRoB3qi7WaAFvL5` or use Flattr:

[![Flattr this](https://button.flattr.com/flattr-badge-large.png)][flattrthis]

[conversations]: https://github.com/siacs/Conversations
[conversations-loc]: https://github.com/siacs/ShareLocationPlugin
[osmdroid]: https://github.com/osmdroid/osmdroid
[mapnik]: http://mapnik.org/
[flattrthis]: https://flattr.com/submit/auto?user_id=SamWhited&url=https%3A%2F%2Fbitbucket.org%2FSamWhited%2Fopensharelocationplugin
