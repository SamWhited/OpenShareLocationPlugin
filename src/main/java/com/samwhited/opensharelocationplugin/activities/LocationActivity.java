package com.samwhited.opensharelocationplugin.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.samwhited.opensharelocationplugin.R;
import com.samwhited.opensharelocationplugin.util.Config;
import com.samwhited.opensharelocationplugin.util.LocationHelper;

public abstract class LocationActivity extends Activity implements LocationListener {
	private LocationManager locationManager;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
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

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
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
	}

	protected SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}
}
