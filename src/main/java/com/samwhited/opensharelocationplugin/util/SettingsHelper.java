package com.samwhited.opensharelocationplugin.util;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

public final class SettingsHelper {
	public static OnlineTileSourceBase getTileProvider(final String provider_name) {
		switch (provider_name) {
			case "MAPNIK":
				return Config.MAPNIK;
			case "CYCLEMAP":
				return Config.CYCLEMAP;
			case "TOPOMAP":
				return Config.TOPO;
			default:
				return Config.MAPNIK;
		}
	}
}
