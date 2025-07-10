package com.atakmap.android.plugintemplate.araaftorPlugin.services;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.atakmap.android.plugintemplate.araaftorPlugin.GeoObject;
import com.atakmap.android.plugintemplate.araaftorPlugin.MediatorApplication;
import com.atakmap.android.plugintemplate.araaftorPlugin.interfaces.AraaftorServiceListener;
import com.atakmap.android.plugintemplate.araaftorPlugin.orientation.Matrix4;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.atakmap.android.plugintemplate.araaftorPlugin.orientation.OrientationCalculator;

public class AraaftorService extends Service implements SensorEventListener, LocationListener, GpsStatus.Listener, OnSharedPreferenceChangeListener {

    private final MediatorApplication context;

    public MediatorApplication getContext() {
        return context;
    }

    private boolean isEnabled = false;
    private boolean networkConnected = false;
    private Location location;
    private ArrayList<AraaftorServiceListener> listeners;
    private double lat = 44.416735;
    private double lon = 8.851644;
    private int gpsSatsAvailable = 0;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 20; // zmiana co 10 m
    private static final long MIN_TIME_BW_UPDATES = 1000 * 100;    // zmiana co minute
    protected LocationManager locationManager;
    private boolean showDialog = false;

    ////SENSOR VARIABLES
    Date lastMeasure;
    float alpha = 0.95f;

    SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorGyroscope;
    private Sensor sensorMagneticField;
    private Sensor sensorLight;
    private float[] valuesAccelerometer;
    private float[] valuesMagneticField;
    private float lightLevel;
    private float[] valuesGyroscope;
    private float[] matrixR;

    /////TRACKING VARIABLES
    public boolean trackingStarted = false;
    public Date dateStarted;
    public Location lastGPSLocation;
    public float distance = 0;
    public float maxSpeed = 0;
    private long lastShakeTime = 0;
    public OrientationCalculator calc;
    private static final float SHAKE_THRESHOLD_GYRO = 1.5f;
    private static final float SHAKE_THRESHOLD_ACCEL = 2.5f;
    private static final int SHAKE_WINDOW_MS = 500;

    public AraaftorService(MediatorApplication context) {
        this.context = context;
        listeners = new ArrayList<>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(this);
        calc = new OrientationCalculator();
        setupLocation();
        setupSensors();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void addmrAIServiceListener(AraaftorServiceListener listener) {
        listeners.add(listener);
        if (showDialog) listener.showSettingsAlert();
    }

    public void removemrAIServiceListener(AraaftorServiceListener listener) {
        listeners.remove(listener);
    }

    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    ////////////////////GPS METHODS
    public Location getLocation() {
        return this.location;
    }

    public Location setupLocation() {
        try {

            if (!checkPermission(context)) return null;
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100000, 20, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100000, 20, locationListener);
            // getting GPS status
            isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            networkConnected = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            Boolean gpsShouldBeUsed = preferences.getBoolean("pref_use_gps", true);
            if (gpsShouldBeUsed) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Log.d("GPSEnabled", "GPS Enabled");
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    locationManager.addGpsStatusListener(gpsStatusListener);

                    if (location != null) {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                        lastGPSLocation = location;
                    }
                }
                if (!isEnabled) {

                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    showDialog = true;
                }
            } else {

                if (!isEnabled && !networkConnected) {
                    // no network provider is enabled
                } else {
                    // First get location from Network Provider
                    if (networkConnected) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    private final GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                int count = 0;
                for (GpsSatellite sat : gpsStatus.getSatellites()) {
                    if (sat.usedInFix()) count++;
                }
                updateGpsSatsAvailable(count);
            }
        }
    };

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
            for (AraaftorServiceListener listener : listeners) {
                listener.onLocationChanged(location);
            }
            List<GeoObject> tempList = context.getScenarioInstance().getScenarioObjects();
            if (tempList.isEmpty()) {
                return;
            }
            context.getScenarioInstance().getScenarioObjects().getFirst().setLocation(new GeoPoint(location.getLatitude(), location.getLongitude()));

            if (trackingStarted) {
                if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                    if (lastGPSLocation != null) distance += location.distanceTo(lastGPSLocation);
                    if (location.getSpeed() > maxSpeed) maxSpeed = location.getSpeed();
                }
            }

            if (location.getProvider().equals(LocationManager.GPS_PROVIDER))
                lastGPSLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    public void updateLocation(Location location) {
        this.location = location;

    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;


        List<GeoObject> tempList = context.getScenarioInstance().getScenarioObjects();
        if (tempList.isEmpty()) {
            return;
        }
        context.getScenarioInstance().getScenarioObjects().getFirst().setLocation(new GeoPoint(location.getLatitude(), location.getLongitude()));

        if (trackingStarted) {
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                if (lastGPSLocation != null) distance += location.distanceTo(lastGPSLocation);
                if (location.getSpeed() > maxSpeed) maxSpeed = location.getSpeed();
            }
            for (AraaftorServiceListener listener : listeners) {
                listener.onLocationChanged(location);
            }
        }

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER))
            lastGPSLocation = location;
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        showDialog = false;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public double getLatitude() {
        if (location != null) {
            lat = location.getLatitude();
        }
        return lat;
    }

    public double getLongitude() {
        if (location != null) {
            lon = location.getLongitude();
        }
        return lon;
    }

    public void showSettingsAlert(final Context context) {
        Log.d("SAME", "called showSettingsAlert");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle("Please enable GPS.");

        // Setting Dialog Message
        alertDialog.setMessage("Due to selecting the Use GPS setting property you need to enable GPS. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();

    }


/////////SENSOR METHODS

    private void setupSensors() {
        matrixR = new float[16];
        valuesAccelerometer = new float[3];
        valuesMagneticField = new float[3];
        valuesGyroscope = new float[3];
        lightLevel = 0f;
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME);
        } else {
            sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            if (sensorLight == null) {
                Log.e("SensorLight", "Brak czujnika światła w tym urządzeniu.");
            }
            sensorManager.registerListener(this, sensorGyroscope, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, sensorLight, SensorManager.SENSOR_DELAY_UI);
        }

        lastMeasure = new Date();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onGpsStatusChanged(final int event) {

        if (checkPermission(context))
            switch (event) {
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    final GpsStatus gs = this.locationManager.getGpsStatus(null);
                    int i = 0;
                    for (GpsSatellite sat : gs.getSatellites()) {
                        if (sat.usedInFix()) i += 1;
                    }
                    this.gpsSatsAvailable = i;
                    break;
            }
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        if (e.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(matrixR, e.values);
            float[] out = new float[3];
            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int screenOrientation = display.getRotation();
            switch (screenOrientation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    break;
                case Surface.ROTATION_90:
                    SensorManager.remapCoordinateSystem(matrixR, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, matrixR);
                    break;
                case Surface.ROTATION_270:
                    SensorManager.remapCoordinateSystem(matrixR, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, matrixR);
                    break;
                default:
                    break;
            }
            Matrix4 newMatrix = new Matrix4();
            newMatrix.set(matrixR);
            float rotation = (float) Math.sqrt(valuesGyroscope[0] * valuesGyroscope[0] + valuesGyroscope[1] * valuesGyroscope[1] + valuesGyroscope[2] * valuesGyroscope[2]);
            calc.getOrientation(newMatrix, screenOrientation, out);
            for (AraaftorServiceListener listener : listeners) {
                listener.onAzimuthChanged(out[0], out[1], out[2], rotation, lightLevel);
            }
        } else {
            switch (e.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    valuesAccelerometer[0] = alpha * valuesAccelerometer[0] + (1 - alpha) * e.values[0];
                    valuesAccelerometer[1] = alpha * valuesAccelerometer[1] + (1 - alpha) * e.values[1];
                    valuesAccelerometer[2] = alpha * valuesAccelerometer[2] + (1 - alpha) * e.values[2];
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    valuesGyroscope[0] = alpha * valuesGyroscope[0] + (1 - alpha) * e.values[0];
                    valuesGyroscope[1] = alpha * valuesGyroscope[1] + (1 - alpha) * e.values[1];
                    valuesGyroscope[2] = alpha * valuesGyroscope[2] + (1 - alpha) * e.values[2];
                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:
                    valuesMagneticField[0] = alpha * valuesMagneticField[0] + (1 - alpha) * e.values[0];
                    valuesMagneticField[1] = alpha * valuesMagneticField[1] + (1 - alpha) * e.values[1];
                    valuesMagneticField[2] = alpha * valuesMagneticField[2] + (1 - alpha) * e.values[2];
                    break;

                case Sensor.TYPE_LIGHT:
                    lightLevel = e.values[0];
                    break;
            }


            if (valuesMagneticField != null && valuesAccelerometer != null && valuesGyroscope != null) {

                boolean success = SensorManager.getRotationMatrix(matrixR, null, valuesAccelerometer, valuesMagneticField);
                if (success) {
                    float[] out = new float[3];

                    Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    int screenOrientation = display.getRotation();
                    switch (screenOrientation) {
                        case Surface.ROTATION_0:
                        case Surface.ROTATION_180:
                            break;
                        case Surface.ROTATION_90:
                            SensorManager.remapCoordinateSystem(matrixR, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, matrixR);
                            break;
                        case Surface.ROTATION_270:
                            SensorManager.remapCoordinateSystem(matrixR, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, matrixR);
                            break;
                        default:
                            break;
                    }
                    Matrix4 newMatrix = new Matrix4();
                    newMatrix.set(matrixR);
                    calc.getOrientation(newMatrix, screenOrientation, out);
                    float rotation = (float) Math.sqrt(valuesGyroscope[0] * valuesGyroscope[0] + valuesGyroscope[1] * valuesGyroscope[1] + valuesGyroscope[2] * valuesGyroscope[2]);
                    for (AraaftorServiceListener listener : listeners) {
                        listener.onAzimuthChanged(out[0], out[1], out[2], rotation, lightLevel);
                    }
                }
            }
        }
    }

    public int getGpsSatsAvailable() {
        return this.gpsSatsAvailable;
    }

    public void updateGpsSatsAvailable(int x) {
        this.gpsSatsAvailable = x;
    }

    @Override
    public void onSharedPreferenceChanged(
            SharedPreferences sharedPreferences, String key) {

        if (key.equals("pref_use_gps")) {
            if (checkPermission(context))
                showDialog = false;
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            locationManager.removeUpdates(this);
            locationManager.removeGpsStatusListener(this);
            setupLocation();
        }
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}

