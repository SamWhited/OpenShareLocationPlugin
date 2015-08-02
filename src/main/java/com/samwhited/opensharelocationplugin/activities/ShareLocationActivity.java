package com.samwhited.opensharelocationplugin.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.samwhited.opensharelocationplugin.R;
import com.samwhited.opensharelocationplugin.overlays.Marker;
import com.samwhited.opensharelocationplugin.overlays.MyLocation;
import com.samwhited.opensharelocationplugin.util.Config;
import com.samwhited.opensharelocationplugin.util.LocationHelper;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class ShareLocationActivity extends LocationActivity implements LocationListener {

	private Location loc;
	private IMapController mapController;
	private Button shareButton;
	private RelativeLayout snackBar;
	private MapView map;
	private boolean marker_fixed_to_loc = true;

	private static final String KEY_LOCATION = "loc";
	private static final String KEY_ZOOM_LEVEL = "zoom";

	@Override
	protected void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable(KEY_LOCATION, this.loc);
		outState.putInt(KEY_ZOOM_LEVEL, map.getZoomLevel());
	}

	@Override
	protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (savedInstanceState.containsKey(KEY_LOCATION)) {
			this.loc = savedInstanceState.getParcelable(KEY_LOCATION);
			if (savedInstanceState.containsKey(KEY_ZOOM_LEVEL)) {
				mapController.setZoom(savedInstanceState.getInt(KEY_ZOOM_LEVEL));
				gotoLoc(false, false);
			} else {
				gotoLoc(true, false);
			}
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_share_location);

		// Get map view and configure it.
		map = (MapView) findViewById(R.id.map);
		map.setTileSource(Config.TILE_SOURCE_PROVIDER);
		map.setBuiltInZoomControls(false);
		map.setMultiTouchControls(true);

		this.mapController = map.getController();
		mapController.setZoom(Config.INITIAL_ZOOM_LEVEL);
		mapController.setCenter(Config.INITIAL_POS);

		// Setup the cancel button
		final Button cancelButton = (Button) findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		// Setup the share button
		this.shareButton = (Button) findViewById(R.id.share_button);
		this.shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				final Intent result = new Intent();

				if (marker_fixed_to_loc && loc != null) {
					result.putExtra("latitude", loc.getLatitude());
					result.putExtra("longitude", loc.getLongitude());
					result.putExtra("altitude", loc.getAltitude());
					result.putExtra("accuracy", (int) loc.getAccuracy());
				} else {
					final IGeoPoint markerPoint = map.getMapCenter();
					result.putExtra("latitude", markerPoint.getLatitude());
					result.putExtra("longitude", markerPoint.getLongitude());
				}

				setResult(RESULT_OK, result);
				finish();
			}
		});

		// Setup the fab button on v21+ devices
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			final ImageButton toggleFixedMarkerButton = (ImageButton) findViewById(R.id.toggle_fixed_marker_button);
			toggleFixedMarkerButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View view) {
					final ImageButton fab = (ImageButton) view;
					toggleFixedLocation();

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							fab.setImageResource(marker_fixed_to_loc ? R.drawable.ic_gps_fixed_white_24dp :
									R.drawable.ic_gps_not_fixed_white_24dp);
							fab.invalidate();
						}
					});
				}
			});
		}

		// Setup the snackbar
		this.snackBar = (RelativeLayout) findViewById(R.id.snackbar);
		final TextView snackbarAction = (TextView) findViewById(R.id.snackbar_action);
		snackbarAction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		});

		requestLocationUpdates();
	}

	@Override
	protected void gotoLoc(final boolean setZoomLevel, final boolean animate) {
		if (this.loc != null && mapController != null) {
			if (setZoomLevel) {
				mapController.setZoom(Config.FINAL_ZOOM_LEVEL);
			}
			if (animate) {
				mapController.animateTo(new GeoPoint(this.loc));
			} else {
				mapController.setCenter(new GeoPoint(this.loc));
			}
		}
	}

	@Override
	protected void gotoLoc() {
		gotoLoc(map.getZoomLevel() == Config.INITIAL_ZOOM_LEVEL, true);
	}

	@Override
	protected void setLoc(final Location location) {
		this.loc = location;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void setShareButtonEnabled(final boolean enabled) {
		if (enabled) {
			this.shareButton.setEnabled(true);
			this.shareButton.setTextColor(0xde000000);
			this.shareButton.setText(R.string.share);
		} else {
			this.shareButton.setEnabled(false);
			this.shareButton.setTextColor(0x8a000000);
			this.shareButton.setText(R.string.locating);
		}
	}

	private void setSnackbarVisibility() {
		if (isLocationEnabled()) {
			this.snackBar.setVisibility(View.GONE);
		} else {
			this.snackBar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setSnackbarVisibility();
		if (this.loc == null) {
			setShareButtonEnabled(false);
		} else {
			updateLocationMarker();
			setShareButtonEnabled(true);
		}
	}

	private void updateLocationMarker() {
		this.map.getOverlays().clear();
		if (this.loc != null) {
			this.map.getOverlays().add(new MyLocation(this, this.loc));
			if (this.marker_fixed_to_loc) {
				map.getOverlays().add(new Marker(this, new GeoPoint(this.loc)));
			} else {
				map.getOverlays().add(new Marker(this));
			}
		}
	}

	@Override
	public void onLocationChanged(final Location location) {
		setSnackbarVisibility();
		if (LocationHelper.isBetterLocation(location, this.loc)) {
			setShareButtonEnabled(true);
			final Location oldLoc = this.loc;
			this.loc = location;

			// Don't jump back to the users location if they're not moving (more or less).
			if (this.marker_fixed_to_loc && oldLoc != null && this.loc.distanceTo(oldLoc) > 1) {
				gotoLoc();
			}

			updateLocationMarker();
		}
	}

	@Override
	public void onStatusChanged(final String provider, final int status, final Bundle extras) {

	}

	@Override
	public void onProviderEnabled(final String provider) {

	}

	@Override
	public void onProviderDisabled(final String provider) {

	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private boolean isLocationEnabledKitkat() {
		try {
			final int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
			return locationMode != Settings.Secure.LOCATION_MODE_OFF;
		} catch (final Settings.SettingNotFoundException e) {
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	private boolean isLocationEnabledLegacy() {
		final String locationProviders = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		return !TextUtils.isEmpty(locationProviders);
	}

	private boolean isLocationEnabled() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			return isLocationEnabledKitkat();
		} else {
			return isLocationEnabledLegacy();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_share_location, menu);
		return true;
	}

	public void toggleFixedLocation() {
		marker_fixed_to_loc = !marker_fixed_to_loc;
		if (marker_fixed_to_loc) {
			gotoLoc();
		}
		updateLocationMarker();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.action_about:
				startActivity(new Intent(this, AboutActivity.class));
				return true;
			case R.id.toggle_fixed_marker_button:
				toggleFixedLocation();
				item.setIcon(marker_fixed_to_loc ? R.drawable.ic_gps_fixed_white_24dp :
						R.drawable.ic_gps_not_fixed_white_24dp);
				item.setTitle(marker_fixed_to_loc ? R.string.action_unfix_from_location :
						R.string.action_fix_to_location);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
