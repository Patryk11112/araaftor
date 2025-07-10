package com.atakmap.android.plugintemplate.araaftorPlugin;

import  android.graphics.Bitmap;
import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.simpleframework.xml.Transient;

import com.atakmap.android.plugintemplate.araaftorPlugin.interfaces.SymbolCacheListener;
import com.atakmap.android.plugintemplate.araaftorPlugin.services.SymbolCacheService;
import com.atakmap.android.plugintemplate.araaftorPlugin.signs.Symbol;

public class GeoObject implements SymbolCacheListener {
    public enum SideOfConflict {FRIENDLY, HOSTILE, NEUTRAL, UNKNOWN}

    protected String name;
    protected GeoPoint location;
    @Transient
    protected Symbol app6aSymbol;
    protected String app6aCode;
    @Transient
    private SymbolCacheService service;
    private boolean released = false;
    private boolean hasBeenReleased = false;
    private SideOfConflict side;
    @Transient
    private Bitmap extendedView;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SideOfConflict getSide() {
        return side;
    }

    public GeoObject() {
        this.service = MediatorApplication.getCacheService();
    }

    public GeoObject(GeoPoint location, String app6aCode) {
        if (app6aCode == null)
            return;
        Log.d("SAME-GO", "Adding geoObject: " + app6aCode);
        this.app6aCode = app6aCode.toLowerCase();
        switch (app6aCode.charAt(1)) {
            case 'f':
                side = SideOfConflict.FRIENDLY;
                break;
            case 'h':
                side = SideOfConflict.HOSTILE;
                break;
            case 'n':
                side = SideOfConflict.NEUTRAL;
                break;
            default:
                side = SideOfConflict.UNKNOWN;
                break;
        }
        this.location = location;
        this.service = MediatorApplication.getCacheService();
        service.postSymbolRequest(this, app6aCode);
    }

    public GeoPoint getLocation() {
        return location;
    }

    public String getApp6aCode() {
        return app6aCode;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public Symbol getApp6aSymbol() {
        released = false;
        if (app6aSymbol != null)
            if (app6aSymbol.getIcon().isRecycled()) {
                service.postSymbolRequest(this, app6aCode);
                return null;
            }
        return app6aSymbol;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
        if (released) {
            if (this.app6aSymbol != null) {
                this.app6aSymbol = null;
                service.notifyFinalize(this, app6aCode);
                hasBeenReleased = true;
            }
        } else {
            if (hasBeenReleased) service.postSymbolRequest(this, app6aCode);
        }
    }

    @Override
    public String toString() {
        return "lat=" + location.getLatitudeE6() + ";lon=" + location.getLongitudeE6() + ";app6a=" + app6aCode;
    }

    @Override
    protected void finalize() {
        if (app6aCode != null) service.notifyFinalize(this, app6aCode);
    }

    @Override
    public void getSymbol(Symbol symbol) {
        if (!released) app6aSymbol = symbol;
        else service.notifyFinalize(this, app6aCode);
    }

    public static boolean checkIfUnit(String app6aCode) {
        return app6aCode.contains("-") && app6aCode.charAt(0) != 'g';
    }

    public Bitmap getExtendedView() {
        return extendedView;
    }

    public void setExtendedView(Bitmap extendedView) {
        this.extendedView = extendedView;
    }

}
