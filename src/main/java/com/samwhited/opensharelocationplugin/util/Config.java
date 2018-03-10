package com.samwhited.opensharelocationplugin.util;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;

public final class Config {
	public final static String LOGTAG = "oslp";
	public final static double INITIAL_ZOOM_LEVEL = 4;
	public final static double FINAL_ZOOM_LEVEL = 15;
	public final static GeoPoint INITIAL_POS = new GeoPoint(33.805278, -84.171389);
	public final static int MY_LOCATION_INDICATOR_SIZE = 10;
	public final static int MY_LOCATION_INDICATOR_OUTLINE_SIZE = 3;
	public final static long LOCATION_FIX_TIME_DELTA = 1000 * 10; // ms
	public final static float LOCATION_FIX_SPACE_DELTA = 10; // m
	final static int LOCATION_FIX_SIGNIFICANT_TIME_DELTA = 1000 * 60 * 2; // ms
	public static final OnlineTileSourceBase PUBLIC_TRANSPORT = new XYTileSource(
			"PublicTransport", 0, 17, 256, ".png",
			new String[] { "http://www.openptmap.org/tiles/" },"Data © OpenStreetMap contributors.");
}
