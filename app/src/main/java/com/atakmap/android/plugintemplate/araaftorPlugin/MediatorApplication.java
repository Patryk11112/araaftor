package com.atakmap.android.plugintemplate.araaftorPlugin;

import android.app.Application;
import android.content.Context;
import android.os.Handler;


import com.atakmap.android.plugintemplate.araaftorPlugin.db.DB;
import com.atakmap.android.plugintemplate.araaftorPlugin.scenario.Scenario;
import com.atakmap.android.plugintemplate.araaftorPlugin.services.SymbolCacheService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import com.atakmap.android.plugintemplate.araaftorPlugin.services.AraaftorService;

public class MediatorApplication extends Application {

    public static boolean RadioSilence = false;

    private static AraaftorService service;
    private static SymbolCacheService cacheService;
    private Scenario scenarioInstance;
    private Handler mHandler;
    private Runnable notificationTimer;

    private static MediatorApplication self;


    public Gson getGson() {
        return gson;
    }
    private DB db;
    private Gson gson;
    private boolean isCommander;


    //TODO !!!!!!!!!!!
    private final String restApiBaseUrl = "http://10.6.80.37:8080"; //TODO <----------
    //TODO !!!!!!!!!!!

    private List<GeoObject> atakObjects = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        db = new DB(this);
        init();
    }

    public void init() {
        service = new AraaftorService(MediatorApplication.this);
        cacheService = new SymbolCacheService();
        cacheService.setMediatorApplication(this);
        self = this;
        mHandler = new Handler();

    }

    public static AraaftorService getService() {
        return service;
    }

    public static SymbolCacheService getCacheService() {
        return cacheService;
    }

    public static Context getAppContext() {
        return self.getApplicationContext();
    }

    public static void kernelPanic() {
        self.onLowMemory();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    public Scenario getScenarioInstance() {
        if (scenarioInstance == null) {
            scenarioInstance = new Scenario(db);
        }
        return scenarioInstance;
    }


    public void loggedIn() {
        notificationTimer = new Runnable() {
            @Override
            public void run() {
                mHandler.postDelayed(this, 1000 * 60);
            }
        };
        notificationTimer.run();
    }


    public List<GeoObject> getAtakObjects() {
        return atakObjects;
    }

    public void addOrUpdateAtakObject(GeoObject geoObject) {
        for (int i = 0; i < atakObjects.size(); i++) {
            if (atakObjects.get(i).getName().equals(geoObject)) {
                atakObjects.get(i).setLocation(geoObject.getLocation());
                return;
            }
        }

        atakObjects.add(geoObject);
    }
}
