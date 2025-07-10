package com.atakmap.android.plugintemplate.araaftorPlugin.araaftorMiniMap;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;

import com.atakmap.android.plugintemplate.plugin.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


public class AraaftorMinimap extends MapView {


    private IMapController mapController;
    private String wmsServer = "https://ows.terrestris.de/osm/service";
    private String wmsLayers = "OSM-WMS";
    private ITileSource tileSource;
    private GeoObjectsOverlay objectsOverlay;
    private Context context;

    public AraaftorMinimap(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
    }

    /**
     * Initializes minimap parameters on ARActivity create
     */
    public void initMiniMap(Location locationC, List<GeoPoint> items, List<String> app6as) {
        this.setClipToOutline(true);
        mapController = getController();
        mapController.setZoom(14);
        mapController.setCenter(new GeoPoint(locationC.getLatitude(), locationC.getLongitude()));
        setMaxZoomLevel((double)17);
        setMinZoomLevel((double)11);


        updateMap(wmsServer, wmsLayers);

        setTileSource(tileSource);
        objectsOverlay = new GeoObjectsOverlay(context, new LinkedList<OverlayItem>(), new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemLongPress(int arg0, OverlayItem arg1) {
                return false;
            }

            @Override
            public boolean onItemSingleTapUp(int arg0, OverlayItem arg1) {
                return true;
            }
        }, items, app6as);
        objectsOverlay.setUnderlay(BitmapFactory.decodeResource(context.getResources(), R.drawable.sign_underlay));
    }

    /**
     * onResume method for ARActivity for the minimap
     */

    public void activityResumed() {
        updateMap(wmsServer, wmsLayers);
        setTileSource(tileSource);
        getOverlays().add(objectsOverlay);
    }

    public void activityPaused() {
        getOverlays().remove(objectsOverlay);
    }

    public void updateMap(String wmsServ, String wmsLays) {
        String sURL = String.format(Locale.US, wmsServ + "?" +
                "LAYERS=" + wmsLays + "&TRANSPARENT=true&FORMAT=image/jpeg&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&EXCEPTIONS=application/vnd.ogc.se_inimage&SRS=EPSG:900913" + "" +
                "&WIDTH=%d&HEIGHT=%d&BBOX=", 256, 256);//EPSG:4326
        tileSource = new WMSTileSource(wmsServ.replace(":", "").replace("/", "") + "_" + wmsLays, 3, 18, 256, ".jpeg", sURL.replace(" ", "%20"));
        Log.d("mCOP", "WMS: " + sURL);
    }
}
