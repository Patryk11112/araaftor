
package com.atakmap.android.plugintemplate;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.RootMapGroupLayer;
import com.atakmap.android.plugintemplate.araaftorPlugin.ElementsOnMap;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.plugintemplate.araaftorPlugin.activities.AraaftorActivity;
import com.atakmap.android.plugintemplate.plugin.R;
import com.atakmap.android.dropdown.DropDown.OnStateListener;
import com.atakmap.android.dropdown.DropDownReceiver;

import com.atakmap.comms.CotDispatcher;
import com.atakmap.coremap.cot.event.CotDetail;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.cot.event.CotPoint;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.time.CoordinatedTime;
import com.atakmap.map.layer.Layer;

import java.util.ArrayList;

public class AraaftorDropDownReceiver extends DropDownReceiver implements
        OnStateListener {

    public static final String TAG = AraaftorDropDownReceiver.class
            .getSimpleName();

    public static final String SHOW_PLUGIN = "com.atakmap.android.plugintemplate.SHOW_PLUGIN";
    private final View templateView;
    private final Context pluginContext;

    private final AraaftorActivity.CameraDataListener cdl = new AraaftorActivity.CameraDataListener();
    private final AraaftorActivity.CameraDataReceiver cdr = new AraaftorActivity.CameraDataReceiver() {
        public void onCameraDataReceived(double lat, double lon, double altitude, String UID, String nameUnit) {
            Log.d(TAG, "" + lat);
            cotEvent(lat, lon, altitude, UID, nameUnit);
        }
    };

    private final AraaftorActivity.DeleteEventDataListener dedl = new AraaftorActivity.DeleteEventDataListener();
    private final AraaftorActivity.DeleteEventDataReceiver dedr = new AraaftorActivity.DeleteEventDataReceiver() {
        public void onDeleteEventDataReceived(String UID) {
            deleteCotEvent(UID);
        }
    };

    /**************************** CONSTRUCTOR *****************************/

    public AraaftorDropDownReceiver(final MapView mapView,
                                    final Context context) {
        super(mapView);
        this.pluginContext = context;

        // Remember to use the PluginLayoutInflator if you are actually inflating a custom view
        // In this case, using it is not necessary - but I am putting it here to remind
        // developers to look at this Inflator
        templateView = PluginLayoutInflater.inflate(context,
                R.layout.main_layout, null);

        cdl.register(getMapView().getContext(), cdr);
        dedl.register(getMapView().getContext(), dedr);
    }


    /**************************** PUBLIC METHODS *****************************/

    public void disposeImpl() {
    }

    /**************************** INHERITED METHODS *****************************/

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        if (action == null)
            return;

        if (action.equals(SHOW_PLUGIN)) {

            Log.d(TAG, "showing plugin drop down");
            showDropDown(templateView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH,
                    HALF_HEIGHT, false, this);
            startMraiActivity();
        }
    }



    @Override
    public void onDropDownSelectionRemoved() {
    }

    @Override
    public void onDropDownVisible(boolean v) {
    }

    @Override
    public void onDropDownSizeChanged(double width, double height) {
    }

    @Override
    public void onDropDownClose() {
    }

    public void cotEvent(double lat, double lon, double altitude, String UID, String nameUnit){

        String unit = decode(nameUnit);
        CotDispatcher internalDispatcher = CotMapComponent.getInternalDispatcher();
        CotEvent markerEvent = new CotEvent();
        CotDetail contact = new CotDetail("contact");
        contact.setAttribute("callsign", "Detect  " + UID);
        CotDetail details = new CotDetail();
        details.addChild(contact);
        markerEvent.setVersion("2.0");
        markerEvent.setDetail(details);
        markerEvent.setUID(UID);
        markerEvent.setTime(new CoordinatedTime());
        markerEvent.setStart(new CoordinatedTime());
        CotPoint markerPoint = new CotPoint(lat, lon, 1, 1, 1);
        markerEvent.setPoint(markerPoint);
        markerEvent.setType(unit);
        internalDispatcher.dispatch(markerEvent);
        cdl.register(getMapView().getContext(), cdr);
        sendAction();
    }
    public String decode(String name){
        String unit = "";
        for (int i = 0; i < name.length(); i++) {
            if (!name.substring(i, i + 1).equals("_")) {
                if (name.charAt(i) == 's' && i == 0) {
                    unit = unit + "a";
                } else if (i == 1) {
                    unit = unit + "-" + name.substring(i, i + 1);
                } else if (i == 2) {
                    unit = unit + "-" + name.substring(i, i + 1).toUpperCase();
                } else if (i == 4) {
                    unit = unit + "-" + name.substring(i, i + 1).toUpperCase();
                    unit = unit + "-" + name.substring(i + 1, i + 2).toUpperCase();
                } else if (i == 6) {
                    unit = unit + "-" + name.substring(i, i + 1).toUpperCase();
                } else if (i == 7) {
                    unit = unit + "-" + name.substring(i, i + 1).toUpperCase();
                }
            }
        }
        return unit;
    }
    public void deleteCotEvent(String UID){
        MapItem item = getMapView().getRootGroup().deepFindUID(UID);

        CotEvent deleteEvent = new CotEvent();
        deleteEvent.setUID(UID);
        deleteEvent.setHow("m-g");
        deleteEvent.setType("t-x-d-d");
        CoordinatedTime currentTime =
                new CoordinatedTime(CoordinatedTime.currentTimeMillis());
        deleteEvent.setStale(currentTime);
        deleteEvent.setStart(currentTime);
        deleteEvent.setTime(currentTime);

        CotDetail deleteDetails = new CotDetail();
        CotDetail linkDetail = new CotDetail("link");
        linkDetail.setAttribute("uid", UID);
        linkDetail.setAttribute("relation", "none");
        linkDetail.setAttribute("type", "none");

        deleteDetails.addChild(linkDetail);
        deleteDetails.addChild(new CotDetail("__forcedelete"));
        deleteEvent.setDetail(deleteDetails);
        Log.d(TAG, "Send Delete Event to others " + deleteEvent);

        MapGroup group = item.getGroup();
        if (group != null)
            group.removeItem(item);
        CotMapComponent.getExternalDispatcher().dispatch(deleteEvent);
        dedl.register(getMapView().getContext(), dedr);
        sendAction();
    }
    public void startMraiActivity() {
        ArrayList<ElementsOnMap> latLonMarkerList = new ArrayList<>();
        ElementsOnMap userMarker = new ElementsOnMap(0.0, 0.0, null);

        for (Layer l : this.getMapView().getLayers(MapView.RenderStack.POINT_OVERLAYS)) {
            for (MapItem item : ((RootMapGroupLayer) l).getSubject().getAllItems()) {
                if (item instanceof Marker) {

                    if (item.getType().equals("self")) {
                        userMarker = new ElementsOnMap(((Marker) item).getPoint().getLatitude(), ((Marker) item).getPoint().getLongitude(), null);
                    }

                    if (item.getTitle() != null && item.getTitle().length() > 0) {
                        if (((Marker) item).getPoint() != null) {
                            latLonMarkerList.add(new ElementsOnMap(((Marker) item).getPoint().getLatitude(), ((Marker) item).getPoint().getLongitude(), ((Marker) item).getIcon().getImageUri(0).toString()));
                        }
                    }
                }
            }
        }

        Intent intent = new Intent();
        intent.setClassName("com.atakmap.android.plugintemplate.plugin",
                "com.atakmap.android.plugintemplate.araaftorPlugin.activities.AraaftorActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("self", userMarker);
        intent.putExtra("markerList", latLonMarkerList);
        getMapView().getContext().startActivity(intent);
    }

    public void sendAction() {
        ArrayList<ElementsOnMap> latLonMarkerList = new ArrayList<>();
        ElementsOnMap userMarker = new ElementsOnMap(0.0, 0.0, null);

        for (Layer l : this.getMapView().getLayers(MapView.RenderStack.POINT_OVERLAYS)) {
            for (MapItem item : ((RootMapGroupLayer) l).getSubject().getAllItems()) {
                if (item instanceof Marker) {

                    if (item.getType().equals("self")) {
                        userMarker = new ElementsOnMap(((Marker) item).getPoint().getLatitude(), ((Marker) item).getPoint().getLongitude(), null);
                    }

                    if (item.getTitle() != null && item.getTitle().length() > 0) {
                        if (((Marker) item).getPoint() != null) {
                            latLonMarkerList.add(new ElementsOnMap(((Marker) item).getPoint().getLatitude(), ((Marker) item).getPoint().getLongitude(), ((Marker) item).getIcon().getImageUri(0).toString()));
                        }
                    }
                }
            }
        }

        Intent intent = new Intent("com.atakmap.android.intent.ACTION_SEND_DATA");
        intent.putExtra("self", userMarker);
        intent.putExtra("markerList", latLonMarkerList);
        getMapView().getContext().sendBroadcast(intent);
    }
}
