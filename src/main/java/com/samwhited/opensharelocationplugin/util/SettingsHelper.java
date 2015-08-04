package com.samwhited.opensharelocationplugin.util;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

public final class SettingsHelper {
	public static OnlineTileSourceBase getTileProvider(final String provider_name) {
		switch (provider_name) {
			case "MAPNIK":
				return TileSourceFactory.MAPNIK;
			case "MAPQUESTOSM":
				return TileSourceFactory.MAPQUESTOSM;
			case "MAPQUESTAERIAL":
				return TileSourceFactory.MAPQUESTAERIAL;
			case "MAPQUESTAERIAL_US":
				return TileSourceFactory.MAPQUESTAERIAL_US;
			case "CYCLEMAP":
				return TileSourceFactory.CYCLEMAP;
			case "PUBLIC_TRANSPORT":
				return TileSourceFactory.PUBLIC_TRANSPORT;
			default:
				return TileSourceFactory.DEFAULT_TILE_SOURCE;
		}
	}
}
