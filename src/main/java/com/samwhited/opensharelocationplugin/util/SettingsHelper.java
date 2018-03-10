package com.samwhited.opensharelocationplugin.util;

import android.content.Context;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.ThunderforestTileSource;

public final class SettingsHelper {
	public static OnlineTileSourceBase getTileProvider(final Context ctx, final String provider_name) {
		switch (provider_name) {
			case "OPEN_STREET_MAP":
			case "MAPNIK":
				return Config.OPEN_STREET_MAP;
			case "CYCLEMAP":
				return new ThunderforestTileSource(ctx, ThunderforestTileSource.CYCLE);
			case "TOPOMAP":
				return Config.TOPO;
			default:
				return Config.OPEN_STREET_MAP;
		}
	}
}
