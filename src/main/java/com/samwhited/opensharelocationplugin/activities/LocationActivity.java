package com.samwhited.opensharelocationplugin.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.samwhited.opensharelocationplugin.R;
import com.samwhited.opensharelocationplugin.overlays.Marker;
import com.samwhited.opensharelocationplugin.overlays.MyLocation;
import com.samwhited.opensharelocationplugin.util.Config;
import com.samwhited.opensharelocationplugin.util.LocationHelper;

import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.Iterator;

public abstract class LocationActivity extends Activity implements LocationListener {
	protected LocationManager locationManager;

	public static final String PREF_SHOW_PUBLIC_TRANSPORT = "pref_show_public_transport";
	public static final String PREF_REQUESTED_PERM_LOCATION = "pref_requested_perm_location";
	public static final String PREF_REQUESTED_PERM_STORAGE = "pref_requested_perm_location";

	public static final int REQUEST_CODE_START_UPDATING = 0;
	public static final int REQUEST_CODE_CREATE = 1;

	private TilesOverlay public_transport_overlay = null;
	private TilesOverlay mapquest_overlay = null;
	protected MapView map = null;

	protected void updateOverlays() {
		Log.d(Config.LOGTAG, "Updating overlays...");
		if (this.map == null) {
			return;
		}

		// If we're using MapQuest Aerial view, overlay the (higher resolution) data if we're in the USA.
		if (map.getTileProvider().getTileSource() == TileSourceFactory.MAPQUESTAERIAL_US &&
				(mapquest_overlay == null || !map.getOverlays().contains(this.mapquest_overlay))) {
			final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getApplicationContext());
			tileProvider.setTileSource(TileSourceFactory.MAPQUESTAERIAL);
			this.mapquest_overlay = new TilesOverlay(tileProvider, getApplicationContext());
			map.getOverlays().add(0, this.mapquest_overlay);
		} else {
			map.getOverlays().remove(this.mapquest_overlay);
		}

		if (map.getOverlays().contains(this.public_transport_overlay)) {
			map.getOverlays().remove(this.public_transport_overlay);
		}
		if (getPreferences().getBoolean(LocationActivity.PREF_SHOW_PUBLIC_TRANSPORT, false)) {
			if (this.public_transport_overlay == null) {
				final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getApplicationContext());
				tileProvider.setTileSource(TileSourceFactory.PUBLIC_TRANSPORT);
				this.public_transport_overlay = new TilesOverlay(tileProvider, getApplicationContext());
			}
			map.getOverlays().add(
					map.getOverlays().contains(this.mapquest_overlay) ? 1 : 0,
					this.public_transport_overlay
			);
		}

		map.invalidate();
	}

	protected void clearMarkers() {
		for (final Iterator<Overlay> iterator = this.map.getOverlays().iterator(); iterator.hasNext(); ) {
			final Overlay overlay = iterator.next();
			if (overlay instanceof Marker || overlay instanceof MyLocation) {
				iterator.remove();
			}
		}
	}

	protected void updateLocationMarkers() {
		clearMarkers();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		updateOverlays();
	}

	protected void gotoLoc() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	protected void gotoLoc(final boolean setZoomLevel, final boolean animate) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	protected abstract void setLoc(final Location location);

	protected void requestLocationUpdates() {
		Log.d(Config.LOGTAG, "Requesting location updates...");
		final Location lastKnownLocationGps;
		final Location lastKnownLocationNetwork;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			requestLocationPermissions(REQUEST_CODE_START_UPDATING);
		}
		try {
			if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
				lastKnownLocationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

				if (lastKnownLocationGps != null) {
					setLoc(lastKnownLocationGps);
				}
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.LOCATION_FIX_TIME_DELTA,
						Config.LOCATION_FIX_SPACE_DELTA, this);
			} else {
				lastKnownLocationGps = null;
			}

			if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
				lastKnownLocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (lastKnownLocationNetwork != null && LocationHelper.isBetterLocation(lastKnownLocationNetwork,
						lastKnownLocationGps)) {
					setLoc(lastKnownLocationNetwork);
				}
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.LOCATION_FIX_TIME_DELTA,
						Config.LOCATION_FIX_SPACE_DELTA, this);
			}

			// If something else is also querying for location more frequently than we are, the battery is already being
			// drained. Go ahead and use the existing locations as often as we can get them.
			if (locationManager.getAllProviders().contains(LocationManager.PASSIVE_PROVIDER)) {
				locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
			}
		} catch (final SecurityException ignored) {
			// This probably won't happen unless the user is on a ROM that allows permission tweaking.
			// TODO: Should we do anything if that is the case?
		}

		try {
			gotoLoc();
		} catch (final UnsupportedOperationException ignored) {
		}
	}

	protected void pauseLocationUpdates() throws SecurityException {
		locationManager.removeUpdates(this);
	}

	protected void setupMenuPrefs(final Menu menu) {
		final MenuItem show_public_transport = menu.findItem(R.id.action_show_public_transport);
		if (show_public_transport != null && show_public_transport.isCheckable()) {
			show_public_transport.setChecked(getPreferences().getBoolean(PREF_SHOW_PUBLIC_TRANSPORT, false));
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.action_show_public_transport:
				if (item.isCheckable()) {
					final boolean checked = !item.isChecked();
					item.setChecked(checked);
					getPreferences().edit().putBoolean(PREF_SHOW_PUBLIC_TRANSPORT, checked).commit();
				}
				updateOverlays();
				return true;
			case R.id.action_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			pauseLocationUpdates();
		} catch (final SecurityException ignored) {
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.setLoc(null);
		requestLocationUpdates();
		updateOverlays();
	}

	@TargetApi(Build.VERSION_CODES.M)
	protected void requestLocationPermissions(final int request_code) {
		final boolean askedForLocationPerm = getPreferences().getBoolean(PREF_REQUESTED_PERM_LOCATION, false);
		final boolean askedForStoragePerm = getPreferences().getBoolean(PREF_REQUESTED_PERM_STORAGE, false);

		if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
				checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

			if (!askedForLocationPerm) {
				requestPermissions(
						new String[]{
								Manifest.permission.ACCESS_FINE_LOCATION,
								Manifest.permission.ACCESS_COARSE_LOCATION
						},
						request_code
				);
			}
			getPreferences().edit().putBoolean(PREF_REQUESTED_PERM_LOCATION, false).apply();
		}
		if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
				checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			if (!askedForStoragePerm) {
				requestPermissions(
						new String[]{
								Manifest.permission.READ_EXTERNAL_STORAGE,
								Manifest.permission.WRITE_EXTERNAL_STORAGE
						},
						request_code
				);
			}
			getPreferences().edit().putBoolean(PREF_REQUESTED_PERM_STORAGE, false).apply();
		}
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode,
	                                       @NonNull final String[] permissions,
	                                       @NonNull final int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		final SharedPreferences.Editor e = getPreferences().edit();
		for (int i = 0; i < grantResults.length; i++) {
			if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[i]) ||
					Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[i])) {
				if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
					e.putBoolean(PREF_REQUESTED_PERM_LOCATION, false);
					requestLocationUpdates();
				} else {
					e.putBoolean(PREF_REQUESTED_PERM_LOCATION, true);
				}
				continue;
			}
			if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(permissions[i]) ||
					Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[i])) {
				if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
					e.putBoolean(PREF_REQUESTED_PERM_STORAGE, false);
				} else {
					e.putBoolean(PREF_REQUESTED_PERM_STORAGE, true);
				}
			}
		}
		e.apply();
	}

	protected SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}
}
