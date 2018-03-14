package com.samwhited.opensharelocationplugin.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import org.osmdroid.util.GeoPoint;

public class ShareLocationActivity extends LocationActivity implements LocationListener {

	private RelativeLayout snackBar;
	private boolean marker_fixed_to_loc = false;
	private MenuItem toggle_fixed_location_item;

	private static final String KEY_FIXED_TO_LOC = "fixed_to_loc";
	private Boolean noAskAgain = false;

	@Override
	protected void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(KEY_FIXED_TO_LOC, marker_fixed_to_loc);
	}

	@Override
	protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (savedInstanceState.containsKey(KEY_FIXED_TO_LOC)) {
			this.marker_fixed_to_loc = savedInstanceState.getBoolean(KEY_FIXED_TO_LOC);
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_share_location);
		setupMapView();

		this.mapController = map.getController();
		mapController.setZoom(Config.INITIAL_ZOOM_LEVEL);
		mapController.setCenter(Config.INITIAL_POS);

		// Setup the cancel button
		final Button cancelButton = findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(view -> {
			setResult(RESULT_CANCELED);
			finish();
		});

		// Setup the snackbar
		this.snackBar = findViewById(R.id.snackbar);
		final TextView snackbarAction = findViewById(R.id.snackbar_action);
		snackbarAction.setOnClickListener(view -> {
			if (isLocationEnabledAndAllowed()) {
				updateUi();
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasLocationPermissions()) {
				requestPermissions(REQUEST_CODE_SNACKBAR_PRESSED);
			} else if (!isLocationEnabled()) {
				startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		});

		// Setup the share button
		final Button shareButton = findViewById(R.id.share_button);
		if (shareButton != null) {
			shareButton.setOnClickListener(view -> {
				final Intent result = new Intent();

				if (marker_fixed_to_loc && myLoc != null) {
					result.putExtra("latitude", myLoc.getLatitude());
					result.putExtra("longitude", myLoc.getLongitude());
					result.putExtra("altitude", myLoc.getAltitude());
					result.putExtra("accuracy", (int) myLoc.getAccuracy());
				} else {
					final IGeoPoint markerPoint = map.getMapCenter();
					result.putExtra("latitude", markerPoint.getLatitude());
					result.putExtra("longitude", markerPoint.getLongitude());
				}

				setResult(RESULT_OK, result);
				finish();
			});
		}

		this.marker_fixed_to_loc = isLocationEnabledAndAllowed();

		// Setup the fab button on v21+ devices
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			final ImageButton toggleFixedMarkerButton = findViewById(R.id.toggle_fixed_marker_button);
			toggleFixedMarkerButton.setOnClickListener(view -> {
				if (!marker_fixed_to_loc) {
					if (!isLocationEnabled()) {
						startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						requestPermissions(REQUEST_CODE_FAB_PRESSED);
					}
				}
				toggleFixedLocation();
			});
		}
	}

	@Override
	public void onRequestPermissionsResult(
			final int requestCode,
			@NonNull final String[] permissions,
			@NonNull final int[] grantResults
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (grantResults.length > 0 &&
				grantResults[0] != PackageManager.PERMISSION_GRANTED &&
				Build.VERSION.SDK_INT >= 23 &&
				permissions.length > 0 &&
				(
						Manifest.permission.LOCATION_HARDWARE.equals(permissions[0]) ||
								Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[0]) ||
								Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[0])
				) &&
				!shouldShowRequestPermissionRationale(permissions[0])) {
			noAskAgain = true;
		}

		if (!noAskAgain && requestCode == REQUEST_CODE_SNACKBAR_PRESSED && !isLocationEnabled() && hasLocationPermissions()) {
			startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		}
		updateUi();
	}

	@Override
	protected void gotoLoc(final boolean setZoomLevel) {
		if (this.myLoc != null && mapController != null) {
			if (setZoomLevel) {
				mapController.setZoom(Config.FINAL_ZOOM_LEVEL);
			}
			mapController.animateTo(new GeoPoint(this.myLoc));
		}
	}

	@Override
	protected void setMyLoc(final Location location) {
		this.myLoc = location;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void updateLocationMarkers() {
		super.updateLocationMarkers();
		if (this.myLoc != null) {
			this.map.getOverlays().add(new MyLocation(this, null, this.myLoc));
			if (this.marker_fixed_to_loc) {
				map.getOverlays().add(new Marker(marker_icon, new GeoPoint(this.myLoc)));
			} else {
				map.getOverlays().add(new Marker(marker_icon));
			}
		} else {
			map.getOverlays().add(new Marker(marker_icon));
		}
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (this.myLoc == null) {
			this.marker_fixed_to_loc = true;
		}
		updateUi();
		if (LocationHelper.isBetterLocation(location, this.myLoc)) {
			final Location oldLoc = this.myLoc;
			this.myLoc = location;

			// Don't jump back to the users location if they're not moving (more or less).
			if (oldLoc == null || (this.marker_fixed_to_loc && this.myLoc.distanceTo(oldLoc) > 1)) {
				gotoLoc();
			}

			updateLocationMarkers();
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

	private boolean isLocationEnabledAndAllowed() {
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || hasLocationPermissions()) && isLocationEnabled();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_share_location, menu);
		super.setupMenuPrefs(menu);
		this.toggle_fixed_location_item = menu.findItem(R.id.toggle_fixed_marker_button);
		updateUi();
		return true;
	}

	private void toggleFixedLocation() {
		this.marker_fixed_to_loc = isLocationEnabledAndAllowed() && !this.marker_fixed_to_loc;
		if (this.marker_fixed_to_loc) {
			gotoLoc(false);
		}
		updateLocationMarkers();
		updateUi();
	}

	@Override
	protected void updateUi() {
		if (noAskAgain || isLocationEnabledAndAllowed()) {
			this.snackBar.setVisibility(View.GONE);
		} else {
			this.snackBar.setVisibility(View.VISIBLE);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			// Setup the fab button on v21+ devices
			final ImageButton fab = findViewById(R.id.toggle_fixed_marker_button);
			if (isLocationEnabledAndAllowed()) {
				fab.setVisibility(View.VISIBLE);
				runOnUiThread(() -> {
					fab.setImageResource(marker_fixed_to_loc ? R.drawable.ic_gps_fixed_white_24dp :
							R.drawable.ic_gps_not_fixed_white_24dp);
					fab.setContentDescription(getResources().getString(
								marker_fixed_to_loc ? R.string.action_unfix_from_location : R.string.action_fix_to_location
								));
					fab.invalidate();
				});
			} else {
				fab.setVisibility(View.GONE);
			}
		} else {
			// Setup the action bar button on < v21 devices
			if (isLocationEnabledAndAllowed()) {
				if (toggle_fixed_location_item != null) {
					toggle_fixed_location_item.setVisible(true);
					runOnUiThread(() -> {
						toggle_fixed_location_item.setIcon(marker_fixed_to_loc ?
								R.drawable.ic_gps_fixed_white_24dp : R.drawable.ic_gps_not_fixed_white_24dp
								);
						toggle_fixed_location_item.setTitle(marker_fixed_to_loc ?
								R.string.action_unfix_from_location : R.string.action_fix_to_location
								);
					});
				}
			} else {
				if (toggle_fixed_location_item != null) {
					toggle_fixed_location_item.setVisible(false);
				}
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.toggle_fixed_marker_button:
				toggleFixedLocation();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
