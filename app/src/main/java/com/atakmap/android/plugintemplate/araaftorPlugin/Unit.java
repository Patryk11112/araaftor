package com.atakmap.android.plugintemplate.araaftorPlugin;

import org.osmdroid.util.GeoPoint;

import java.util.LinkedList;

public class Unit extends GeoObject {
    private int id;
    private String guid;
    private LinkedList<Unit> subordinates = new LinkedList<>();
    private Unit superior = null;
    private int[] X;
    private boolean group = false;
    private int superiorResId = 0;
    private double[] latlon = new double[2];
    private String[] latlonStrings = new String[4];
    private double thl = 0;

    public double getThl() {
        return thl;
    }

    public boolean hasSuperior() {
        return superior != null;
    }

    public int getSuperiorResId() {
        return superiorResId;
    }

    public void setSuperiorResId(int id) {
        superiorResId = id;
    }

    public Unit getSuperior() {
        return superior;
    }

    public void setSuperior(Unit superior) {
        this.superior = superior;
    }

    public LinkedList<Unit> getSubordinates() {
        return subordinates;
    }

    public Unit(String name) {
        this.name = name;
        this.group = true;
        X = new int[5];
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Unit(String name, GeoPoint location, String app6aCode, int id, String guid) {
        super(location, app6aCode);
        this.guid = guid;
        this.name = name;
        this.id = id;
        X = new int[5];
        latlon[0] = this.getLocation().getLatitudeE6() / 1000000.0D;
        latlon[1] = this.getLocation().getLongitudeE6() / 1000000.0D;
        latlonStrings = Util.getLatLonString(latlon, 1);
    }

    public int getId() {
        return id;
    }


    public Unit(String name, GeoPoint location, String app6aCode, LinkedList<Unit> subordinates, int id) {
        super(location, app6aCode);
        this.name = name;
        this.id = id;
        this.subordinates = subordinates;
        X = new int[5];
        latlon[0] = this.getLocation().getLatitudeE6() / 1000000.0D;
        latlon[1] = this.getLocation().getLongitudeE6() / 1000000.0D;
        latlonStrings = Util.getLatLonString(latlon, 1);
    }

    public int[] getX() {
        return X;
    }

    public double[] getLatlon() {
        return latlon;
    }

    public String[] getLatlonStrings() {
        return latlonStrings;
    }

    public String getLatlonString(int i) {
        return latlonStrings[i];
    }

    public void addSubordinate(Unit unit) {
        subordinates.add(unit);
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
}
