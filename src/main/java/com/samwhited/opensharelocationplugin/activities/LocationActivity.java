package com.samwhited.opensharelocationplugin.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
	private LocationManager locationManager;

	public static final String PREF_SHOW_PUBLIC_TRANSPORT = "pref_show_public_transport";

	private TilesOverlay public_transport_overlay = null;
	private TilesOverlay mapquest_overlay = null;
	protected MapView map = null;

	protected void updateOverlays() {
		if (this.map == null) {
			return;
		}

		// If we're using MapQuest Aerial view, overlay the (higher resolution) data if we're in the USA.
		if (map.getTileProvider().getTileSource() == TileSourceFactory.MAPQUESTAERIAL_US &&
				(mapquest_overlay == null || !map.getOverlays().contains(this.mapquest_overlay))) {
			final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getApplicationContext());
			tileProvider.setTileSource(TileSourceFactory.MAPQUESTAERIAL);
			this.mapquest_overlay = new TilesOverlay(tileProvider, getApplicationContext());
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
			map.getOverlays().add(this.public_transport_overlay);
		}
	}

	protected void clearMarkers() {
		for (final Iterator<Overlay> iterator = this.map.getOverlays().iterator(); iterator.hasNext();) {
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
		final Location lastKnownLocationGps;
		final Location lastKnownLocationNetwork;

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

		try {
			gotoLoc();
		} catch (final UnsupportedOperationException ignored) {
		}
	}

	protected void pauseLocationUpdates() {
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
		pauseLocationUpdates();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.setLoc(null);

		requestLocationUpdates();
		updateOverlays();
	}

	protected SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}
}
