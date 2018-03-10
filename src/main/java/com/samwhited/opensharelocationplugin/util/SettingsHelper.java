package com.samwhited.opensharelocationplugin.util;

import android.content.Context;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.ThunderforestTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

public final class SettingsHelper {
	public static OnlineTileSourceBase getTileProvider(final Context ctx, final String provider_name) {
		switch (provider_name) {
			case "CYCLEMAP":
				return new ThunderforestTileSource(ctx, ThunderforestTileSource.CYCLE);
			case "TOPOMAP":
				return TileSourceFactory.OpenTopo;
			case "OPEN_STREET_MAP":
			default:
				return new XYTileSource("OpenStreetMap",
						0, 19, 256, ".png", new String[] {
							"https://a.tile.openstreetmap.org/",
							"https://b.tile.openstreetmap.org/",
							"https://c.tile.openstreetmap.org/" },"© OpenStreetMap contributors");
		}
	}
}
