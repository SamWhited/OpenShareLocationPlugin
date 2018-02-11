package com.samwhited.opensharelocationplugin.util;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

public final class SettingsHelper {
	public static OnlineTileSourceBase getTileProvider(final String provider_name) {
		switch (provider_name) {
			case "OPEN_STREET_MAP":
			case "MAPNIK":
				return Config.OPEN_STREET_MAP;
			case "CYCLEMAP":
				return Config.CYCLEMAP;
			case "TOPOMAP":
				return Config.TOPO;
			default:
				return Config.OPEN_STREET_MAP;
		}
	}
}
