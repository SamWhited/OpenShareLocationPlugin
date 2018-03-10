package com.samwhited.opensharelocationplugin.util;

import com.samwhited.opensharelocationplugin.BuildConfig;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;

public final class Config {
	public final static String LOGTAG = "oslp";
	public final static int INITIAL_ZOOM_LEVEL = 4;
	public final static int FINAL_ZOOM_LEVEL = 15;
	public final static GeoPoint INITIAL_POS = new GeoPoint(33.805278, -84.171389);
	public final static int MY_LOCATION_INDICATOR_SIZE = 10;
	public final static int MY_LOCATION_INDICATOR_OUTLINE_SIZE = 3;
	public final static long LOCATION_FIX_TIME_DELTA = 1000 * 10; // ms
	public final static float LOCATION_FIX_SPACE_DELTA = 10; // m
	final static int LOCATION_FIX_SIGNIFICANT_TIME_DELTA = 1000 * 60 * 2; // ms

	static final OnlineTileSourceBase OPEN_STREET_MAP = new XYTileSource("OpenStreetMap",
			0, 19, 256, ".png", new String[] {
			"https://a.tile.openstreetmap.org/",
			"https://b.tile.openstreetmap.org/",
			"https://c.tile.openstreetmap.org/" },"© OpenStreetMap contributors");
	static final OnlineTileSourceBase CYCLEMAP = new OnlineTileSourceBase(
			"CycleMap",
			0, 22, 256,
			".png",
			new String[] {
					"https://a.tile.thunderforest.com/cycle/",
					"https://b.tile.thunderforest.com/cycle/",
					"https://c.tile.thunderforest.com/cycle/",
			},
			"Maps © Thunderforest, Data © OpenStreetMap contributors.") {
		@Override
		public String getTileURLString(final MapTile aTile) {
			return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getX() + "/" + aTile.getY()
					+ mImageFilenameEnding + "?apikey=" + BuildConfig.THUNDERFOREST_API_KEY;
		}
	};
	static final OnlineTileSourceBase TOPO = TileSourceFactory.OpenTopo;
	public static final OnlineTileSourceBase PUBLIC_TRANSPORT = new XYTileSource(
			"PublicTransport", 0, 17, 256, ".png",
			new String[] { "http://www.openptmap.org/tiles/" },"Data © OpenStreetMap contributors.");
}
