package com.atakmap.android.plugintemplate.araaftorPlugin.interfaces;

import android.location.Location;

public interface AraaftorServiceListener {
    void onLocationChanged(Location location);

    void onAzimuthChanged(float azimuth, float pitch, float roll, float gyroscope, float lightLevel);

    void showSettingsAlert();
}
