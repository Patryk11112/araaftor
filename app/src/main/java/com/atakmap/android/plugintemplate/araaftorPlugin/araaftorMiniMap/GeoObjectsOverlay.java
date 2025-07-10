package com.atakmap.android.plugintemplate.araaftorPlugin.araaftorMiniMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.atakmap.android.plugintemplate.araaftorPlugin.GeoObject;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import java.util.List;


public class GeoObjectsOverlay extends ItemizedIconOverlay<OverlayItem> {
    private final List<GeoPoint> list;
    private boolean showDetails = false;
    private Bitmap underlay = null;

    Context context;
    private final List<String> app6as;

    public GeoObjectsOverlay(
            Context pContext,
            List<OverlayItem> pList,
            OnItemGestureListener<OverlayItem> pOnItemGestureListener,
            List<GeoPoint> list, List<String> app6as) {
        super(pContext, pList, pOnItemGestureListener);
        this.list = list;
        this.context = pContext;
        // TODO Auto-generated constructor stub
        this.app6as = app6as;
    }

    public void setUnderlay(Bitmap bitmap) {
        underlay = bitmap;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        this.mItemList.clear();
        removeAllItems();
        for (int i = 0; i < list.size(); i++) {
            GeoPoint geoObj = list.get(i);
            if (isGeoObjectInArea(geoObj, mapView, canvas)) {
                Log.d("mCOP", "Object in area: " + geoObj.toString());

                String temp = app6as.get(i).replace("-", "_").replace("asset://mil_std_2525c/", "").replace(".png", "");
                int id = this.context.getResources().getIdentifier(temp, "drawable", this.context.getPackageName());
                Drawable drawable = this.context.getResources().getDrawable(id);
                Log.d("mCOP-GeoObjectsOverlay", "Adding item");
                OverlayItem item = new OverlayItem(geoObj.toString(), temp, geoObj);
                item.setMarkerHotspot(HotspotPlace.BOTTOM_CENTER);
                item.setMarker(drawable);
                addItem(item);
            }

        }
        super.draw(canvas, mapView, false);
    }

    private boolean isGeoObjectInArea(GeoPoint geoObj, MapView mapView, Canvas canvas) {//, GeoPoint[] cornerCoords) {
        return mapView.getProjection().getBoundingBox().contains(geoObj);
    }

    public int getSideColor(GeoObject.SideOfConflict side) {
        if (side != null) {
            if (side == GeoObject.SideOfConflict.FRIENDLY) {
                return Color.rgb(146, 204, 227);
            } else if (side == GeoObject.SideOfConflict.HOSTILE) {
                return Color.rgb(233, 129, 125);
            } else if (side == GeoObject.SideOfConflict.NEUTRAL) {
                return Color.rgb(245, 241, 132);
            } else if (side == GeoObject.SideOfConflict.UNKNOWN) {
                return Color.rgb(171, 215, 162);
            } else return Color.WHITE;
        } else return Color.WHITE;
    }
}
