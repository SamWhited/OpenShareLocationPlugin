package com.samwhited.opensharelocationplugin.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.samwhited.opensharelocationplugin.R;
import com.samwhited.opensharelocationplugin.overlays.Marker;
import com.samwhited.opensharelocationplugin.overlays.MyLocation;
import com.samwhited.opensharelocationplugin.util.Config;
import com.samwhited.opensharelocationplugin.util.LocationHelper;
import com.samwhited.opensharelocationplugin.util.SettingsHelper;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;

public abstract class LocationActivity extends AppCompatActivity implements LocationListener {
	protected LocationManager locationManager;

	public static final String PREF_SHOW_PUBLIC_TRANSPORT = "pref_show_public_transport";

	public static final int REQUEST_CODE_CREATE = 0;
	public static final int REQUEST_CODE_FAB_PRESSED = 1;
	public static final int REQUEST_CODE_SNACKBAR_PRESSED = 2;

	protected static final String KEY_LOCATION = "loc";
	protected static final String KEY_ZOOM_LEVEL = "zoom";

	private TilesOverlay public_transport_overlay = null;
	protected Location myLoc = null;
	protected MapView map = null;
	protected IMapController mapController = null;

protected Bitmap marker_icon;

	protected void updateOverlays() {
		Log.d(Config.LOGTAG, "Updating overlays...");
		if (this.map == null) {
			Log.d(Config.LOGTAG, "No map found, failed to update overlays.");
			return;
		}

		if (getPreferences().getBoolean(LocationActivity.PREF_SHOW_PUBLIC_TRANSPORT, false)) {
			if (this.public_transport_overlay == null) {
				final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getApplicationContext());
				tileProvider.setTileSource(Config.PUBLIC_TRANSPORT);
				this.public_transport_overlay = new TilesOverlay(tileProvider, getApplicationContext());
			}
			if (!map.getOverlays().contains(this.public_transport_overlay)) {
				map.getOverlays().add(this.public_transport_overlay);
			}
		} else if (map.getOverlays().contains(this.public_transport_overlay)){
			map.getOverlays().remove(this.public_transport_overlay);
		}

		map.invalidate();
	}

	protected void clearMarkers() {
		synchronized (this.map.getOverlays()) {
			for (final Overlay overlay : this.map.getOverlays()) {
				if (overlay instanceof Marker || overlay instanceof MyLocation) {
					this.map.getOverlays().remove(overlay);
				}
			}
		}
	}

	protected void updateLocationMarkers() {
		clearMarkers();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Ask for location permissions if location services are enabled and we're just starting the activity
		// (we don't want to keep pestering them on every screen rotation or if there's no point because it's disabled
		// anyways).
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && savedInstanceState == null) {
			if (isLocationEnabled()) {
				requestPermissions(REQUEST_CODE_CREATE);
			}
		}

		final Context ctx = getApplicationContext();
		final IConfigurationProvider config = Configuration.getInstance();
		config.load(ctx, getPreferences());

		final File f = new File(getApplicationContext().getCacheDir()+ "/tiles");
		try {
			//noinspection ResultOfMethodCallIgnored
			f.mkdirs();
		} catch (final SecurityException ignored) {
		}
		if (f.exists() && f.isDirectory() && f.canRead() && f.canWrite()) {
			Log.d(Config.LOGTAG, "Setting tile cache at: " + f.getAbsolutePath());
			config.setOsmdroidTileCache(f.getAbsoluteFile());
		}

		this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		this.marker_icon = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.marker);
		updateOverlays();
	}

	@Override
	protected void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);

		final IGeoPoint center = map.getMapCenter();
		outState.putParcelable(KEY_LOCATION, new GeoPoint(
				center.getLatitude(),
				center.getLongitude()
		));
		outState.putDouble(KEY_ZOOM_LEVEL, map.getZoomLevelDouble());
	}

	@Override
	protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (savedInstanceState.containsKey(KEY_LOCATION)) {
			mapController.setCenter(savedInstanceState.getParcelable(KEY_LOCATION));
		}
		if (savedInstanceState.containsKey(KEY_ZOOM_LEVEL)) {
			mapController.setZoom(savedInstanceState.getDouble(KEY_ZOOM_LEVEL));
		}
	}

	protected void setupMapView() {
		// Get map view and configure it.
		map = findViewById(R.id.map);
		map.setTileSource(SettingsHelper.getTileProvider(getApplicationContext(), getPreferences().getString("tile_provider", "OPEN_STREET_MAP")));
		map.setBuiltInZoomControls(false);
		map.setMultiTouchControls(true);
		map.setTilesScaledToDpi(getPreferences().getBoolean("scale_tiles_for_high_dpi", false));
	}

	protected void gotoLoc() {
		gotoLoc(map.getZoomLevelDouble() == Config.INITIAL_ZOOM_LEVEL);
	}

	protected abstract void gotoLoc(final boolean setZoomLevel);

	protected abstract void setMyLoc(final Location location);

	protected void requestLocationUpdates() {
		Log.d(Config.LOGTAG, "Requesting location updates...");
		final Location lastKnownLocationGps;
		final Location lastKnownLocationNetwork;

		try {
			if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
				lastKnownLocationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

				if (lastKnownLocationGps != null) {
					setMyLoc(lastKnownLocationGps);
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
					setMyLoc(lastKnownLocationNetwork);
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
					getPreferences().edit().putBoolean(PREF_SHOW_PUBLIC_TRANSPORT, checked).apply();
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
		Configuration.getInstance().save(this, getPreferences());
		map.onPause();
		try {
			pauseLocationUpdates();
		} catch (final SecurityException ignored) {
		}
	}

	protected abstract void updateUi();

	protected boolean mapAtInitialLoc() {
		return map.getZoomLevelDouble() == Config.INITIAL_ZOOM_LEVEL;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Configuration.getInstance().load(this, getPreferences());
		map.onResume();
		this.setMyLoc(null);
		requestLocationUpdates();
		updateOverlays();
		updateLocationMarkers();
		updateUi();
		map.setTileSource(SettingsHelper.getTileProvider(getApplicationContext(), getPreferences().getString("tile_provider", "OPEN_STREET_MAP")));
		map.setTilesScaledToDpi(getPreferences().getBoolean("scale_tiles_for_high_dpi", false));

		if (mapAtInitialLoc()) {
			gotoLoc();
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	protected boolean hasLocationPermissions() {
		return (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
				checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
	}

	@TargetApi(Build.VERSION_CODES.M)
	protected void requestPermissions(final int request_code) {
		if (!hasLocationPermissions()) {
			requestPermissions(
					new String[]{
							Manifest.permission.ACCESS_FINE_LOCATION,
							Manifest.permission.ACCESS_COARSE_LOCATION,
					},
					request_code
			);
		}
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode,
	                                       @NonNull final String[] permissions,
	                                       @NonNull final int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		for (int i = 0; i < grantResults.length; i++) {
			if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[i]) ||
					Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[i])) {
				if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
					requestLocationUpdates();
				}
			}
		}
	}

	protected SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private boolean isLocationEnabledKitkat() {
		try {
			final int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
			return locationMode != Settings.Secure.LOCATION_MODE_OFF;
		} catch( final Settings.SettingNotFoundException e ){
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	private boolean isLocationEnabledLegacy() {
		final String locationProviders = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		return !TextUtils.isEmpty(locationProviders);
	}

	protected boolean isLocationEnabled() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			return isLocationEnabledKitkat();
		} else {
			return isLocationEnabledLegacy();
		}
	}
}
