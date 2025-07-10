package com.atakmap.android.plugintemplate.araaftorPlugin;

import android.view.View;

import org.osmdroid.util.GeoPoint;

public class AraaftorMarker {

    private View view;
    private GeoObject object;
    private GeoPoint point;
    private int pointIndex;
    private String app6a;

    public String getApp6a() {
        return app6a;
    }

    public void setApp6a(String app6a) {
        this.app6a = app6a;
    }

    public AraaftorMarker(GeoObject obj) {
        object = obj;
        point = null;
    }

    public AraaftorMarker(GeoPoint obj, int index) {
        point = obj;
        pointIndex = index;
        object = null;
    }

    public AraaftorMarker(GeoPoint obj, int index, String app6a) {
        point = obj;
        pointIndex = index;
        object = null;
        this.app6a = app6a;
    }

    public View getView() {
        return view;
    }

    public void setView(View view2) {
        this.view = view2;
    }

    public GeoObject getObject() {
        return object;
    }

    public GeoPoint getPoint() {
        return point;
    }

    public int getPointIndex() {
        return pointIndex;
    }


    @Override
    public void finalize() {
        if (object != null) object.setReleased(true);
    }

}
