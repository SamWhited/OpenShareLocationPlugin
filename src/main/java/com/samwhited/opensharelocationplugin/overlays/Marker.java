package com.samwhited.opensharelocationplugin.overlays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;

import com.samwhited.opensharelocationplugin.R;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

/**
 * An immutable marker overlay.
 */
public class Marker extends SimpleLocationOverlay {
	private final GeoPoint position;
	private final Bitmap icon;
	private final Point mapPoint;

	/**
	 * Create a marker overlay which will be drawn at the current Geographical position.
	 * @param ctx The context
	 * @param position The geographic position where the marker will be drawn (if it is inside the view)
	 */
	public Marker(final Context ctx, final GeoPoint position) {
		super(ctx);
		this.position = position;
		this.icon = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.marker);
		this.mapPoint = new Point();
	}

	/**
	 * Create a marker overlay which will be drawn centered in the view.
	 * @param ctx The context
	 */
	public Marker(final Context ctx) {
		this(ctx, null);
	}

	@Override
	public void draw(final Canvas c, final MapView view, final boolean shadow) {
		super.draw(c, view, shadow);

		// If no position was set for the marker, draw it centered in the view.
		view.getProjection().toPixels(this.position == null ? view.getMapCenter() : position, mapPoint);

		c.drawBitmap(icon,
				mapPoint.x - icon.getWidth() / 2,
				mapPoint.y - icon.getHeight(),
				null);

	}
}
