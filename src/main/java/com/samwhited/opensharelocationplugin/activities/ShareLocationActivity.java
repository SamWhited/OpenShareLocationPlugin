package com.samwhited.opensharelocationplugin.activities;

import android.content.Intent;
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
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;

public class ShareLocationActivity extends LocationActivity implements LocationListener {

	private Location loc;
	private IMapController mapController;
	private RelativeLayout snackBar;
	private boolean marker_fixed_to_loc = false;
	private MenuItem toggle_fixed_location_item;

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
		setupMapView();

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

		// Setup the snackbar
		this.snackBar = (RelativeLayout) findViewById(R.id.snackbar);
		final TextView snackbarAction = (TextView) findViewById(R.id.snackbar_action);
		snackbarAction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {

				if (isLocationEnabledAndAllowed()) {
					updateLocationUi();
				} else if (!hasLocationPermissions()) {
					requestPermissions(REQUEST_CODE_SNACKBAR_PRESSED);
				} else if (!isLocationEnabled()) {
					startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			}
		});

		// Setup the share button
		final Button shareButton = (Button) findViewById(R.id.share_button);
		if (shareButton != null) {
			shareButton.setOnClickListener(new View.OnClickListener() {
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
		}

		this.marker_fixed_to_loc = isLocationEnabledAndAllowed();

		// Setup the fab button on v21+ devices
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			final ImageButton toggleFixedMarkerButton = (ImageButton) findViewById(R.id.toggle_fixed_marker_button);
			toggleFixedMarkerButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View view) {
					if (!marker_fixed_to_loc) {
						if (!isLocationEnabled()) {
							startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
						} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
							requestPermissions(REQUEST_CODE_FAB_PRESSED);
						}
					}
					toggleFixedLocation();
				}
			});
		}

		// Don't request permissions over and over if we rotated the screen
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && savedInstanceState == null) {
			requestPermissions(REQUEST_CODE_CREATE);
		}

		updateLocationUi();
		requestLocationUpdates();
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode,
	                                       @NonNull final String[] permissions,
	                                       @NonNull final int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_CODE_SNACKBAR_PRESSED && !isLocationEnabled() && hasLocationPermissions()) {
			startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		}
		updateLocationUi();
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

	@Override
	protected void onResume() {
		super.onResume();
		updateLocationUi();
		updateLocationMarkers();
	}

	@Override
	protected void updateLocationMarkers() {
		super.updateLocationMarkers();
		if (this.loc != null) {
			this.map.getOverlays().add(new MyLocation(this, this.loc));
			if (this.marker_fixed_to_loc) {
				map.getOverlays().add(new Marker(this, new GeoPoint(this.loc)));
			} else {
				map.getOverlays().add(new Marker(this));
			}
		} else {
			map.getOverlays().add(new Marker(this));
		}
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (this.loc == null) {
			this.marker_fixed_to_loc = true;
		}
		updateLocationUi();
		if (LocationHelper.isBetterLocation(location, this.loc)) {
			final Location oldLoc = this.loc;
			this.loc = location;

			// Don't jump back to the users location if they're not moving (more or less).
			if (oldLoc == null || (this.marker_fixed_to_loc && this.loc.distanceTo(oldLoc) > 1)) {
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
		updateLocationUi();
		return true;
	}

	private void toggleFixedLocation() {
		setFixedLocation(!this.marker_fixed_to_loc);
	}

	private void setFixedLocation(final boolean marker_fixed_to_loc) {
		this.marker_fixed_to_loc = isLocationEnabledAndAllowed() && marker_fixed_to_loc;
		if (marker_fixed_to_loc) {
			gotoLoc();
		}
		updateLocationMarkers();
		updateLocationUi();
	}

	private void updateLocationUi() {
		if (isLocationEnabledAndAllowed()) {
			this.snackBar.setVisibility(View.GONE);
		} else {
			this.snackBar.setVisibility(View.VISIBLE);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			// Setup the fab button on v21+ devices
			final ImageButton fab = (ImageButton) findViewById(R.id.toggle_fixed_marker_button);
			if (isLocationEnabledAndAllowed()) {
				fab.setVisibility(View.VISIBLE);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						fab.setImageResource(marker_fixed_to_loc ? R.drawable.ic_gps_fixed_white_24dp :
								R.drawable.ic_gps_not_fixed_white_24dp);
						fab.setContentDescription(getResources().getString(
									marker_fixed_to_loc ? R.string.action_unfix_from_location : R.string.action_fix_to_location
									));
						fab.invalidate();
					}
				});
			} else {
				fab.setVisibility(View.GONE);
			}
		} else {
			// Setup the action bar button on < v21 devices
			if (isLocationEnabledAndAllowed()) {
				if (toggle_fixed_location_item != null) {
					toggle_fixed_location_item.setVisible(true);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							toggle_fixed_location_item.setIcon(marker_fixed_to_loc ?
									R.drawable.ic_gps_fixed_white_24dp : R.drawable.ic_gps_not_fixed_white_24dp
									);
							toggle_fixed_location_item.setTitle(marker_fixed_to_loc ?
									R.string.action_unfix_from_location : R.string.action_fix_to_location
									);
						}
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
