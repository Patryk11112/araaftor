package com.atakmap.android.plugintemplate.araaftorPlugin.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.atakmap.android.plugintemplate.araaftorPlugin.signs.Symbol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;

import com.atakmap.android.plugintemplate.araaftorPlugin.MediatorApplication;
import com.atakmap.android.plugintemplate.araaftorPlugin.interfaces.SymbolCacheListener;

public class SymbolCacheService extends Service {
    private boolean countRequests = false;
    private int requested = 0;
    private int provided = 0;
    private boolean showedDialog = false;
    private MediatorApplication mediatorApplication;

    private String filePath;
    private Hashtable<String, LinkedList<SymbolCacheListener>> signQueues;
    private Hashtable<String, SymbolContainer> bitmapMap;
    private Hashtable<String, Integer> mayBeRecycled;

    public SymbolCacheService() {
        signQueues = new Hashtable<>();
        bitmapMap = new Hashtable<>();
        mayBeRecycled = new Hashtable<>();
    }

    public Hashtable<String, LinkedList<SymbolCacheListener>> getSignQueues() {
        return signQueues;
    }

    public void cacheSymbol(String size, Symbol symbol) {
        String cacheDirectory = getCacheDirectory();
        File dir = new File(cacheDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileCode = size + symbol.getCode().replace("/", "");

        File file = new File(dir, fileCode + ".png");
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            symbol.getIcon().compress(Bitmap.CompressFormat.PNG, 85, fOut);
            addToList(fileCode, symbol);
            fOut.flush();
            fOut.close();
            symbolCached(size, symbol);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        //////WHEN CALLER RESIGNS FROM SYMBOL BEFORE IT IS SERVED THE SYMBOL IS SAVED TO HARD DRIVE IF NECESSARY
        if (mayBeRecycled.contains(fileCode)) {
            int x = mayBeRecycled.get(fileCode);
            mayBeRecycled.remove(fileCode);
            bitmapMap.get(fileCode).decreaseCount(x);
        }
    }

    /////GET SYMBOL FROM SERVICE
    public synchronized void postSymbolRequest(SymbolCacheListener caller, String app6aCode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MediatorApplication.getAppContext());
        String size = preferences.getString("pref_symbol_size", "150");
        Bitmap icon = null;
        app6aCode = app6aCode.toLowerCase();
        String fileCode = size + app6aCode;
        ////SIGN IS IN LOCAL CACHE
        if (bitmapMap.containsKey(fileCode)) {
            bitmapMap.get(fileCode).increaseCount();
            caller.getSymbol(bitmapMap.get(fileCode).symbol);
            Log.d("SAME", "IS IN LOCAL MEMORY :D");
        }

        ///SIGN IS ON SD
        else if (isSymbolCached(fileCode)) {
            icon = BitmapFactory.decodeFile(filePath);
            Log.d("SAME", icon.toString());
            Log.d("SAME", "FOUND IN FILE SYSTEM");
            Symbol symbol = new Symbol(app6aCode, icon);
            addToList(fileCode, symbol);
            caller.getSymbol(symbol);
        } else {
            //////SIGN IS BEING DOWNLOADED
            if (signQueues.containsKey(fileCode)) {
                signQueues.get(fileCode).add(caller);
            } else {

                signQueues.put(fileCode, new LinkedList<SymbolCacheListener>());
                signQueues.get(fileCode).add(caller);
                if (countRequests) {
                    requested++;
                    updateNotification();
                }
            }
        }
    }

    private void symbolCached(String size, Symbol symbol) {
        String fileCode = size + symbol.getCode();
        addToList(fileCode, symbol);
        bitmapMap.get(fileCode).increaseCount(signQueues.size() - 1);
        Log.d("mCOP", "Symbol check " + (symbol == null) + " symbol code: " + (fileCode));
        String signQueuesKey = "KeySet:=";
        for (String key : signQueues.keySet()) {
            signQueuesKey += "{" + key + "}";
        }
        Log.d("mCOP", signQueuesKey);
        signQueues.get(fileCode).clear();
        signQueues.remove(fileCode);
        provided++;
        updateNotification();

    }

    public void removeFromList(SymbolContainer sc) {
        bitmapMap.remove(sc.getFileCode());
    }

    public void addToList(String fileCode, Symbol sc) {
        bitmapMap.put(fileCode, new SymbolContainer(this, sc, fileCode));
    }

    public void notifyFinalize(SymbolCacheListener caller, String app6aCode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MediatorApplication.getAppContext());
        String size = preferences.getString("pref_symbol_size", "150");
        String fileCode = size + app6aCode;
        if (bitmapMap.containsKey(fileCode))
            bitmapMap.get(fileCode).decreaseCount();
        else {
            if (signQueues.contains(fileCode)) {
                signQueues.get(fileCode).remove(caller);
                int x = (mayBeRecycled.contains(fileCode)) ? mayBeRecycled.get(fileCode) : 0;
                mayBeRecycled.put(fileCode, x + 1);
            }
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void stopCounting() {
        countRequests = false;
    }

    public void updateNotification() {
    }

    public void setMediatorApplication(MediatorApplication mediatorApplication) {
        this.mediatorApplication = mediatorApplication;
    }

    private boolean isSymbolCached(String app6aCode) {
        filePath = getCacheDirectory() + getSymbolFileName(app6aCode);
        File file = new File(filePath);

        return file.exists();
    }

    @NonNull
    private String getSymbolFileName(String app6aCode) {
        return app6aCode.replace("/", "") + ".png";
    }

    @NonNull
    private String getCacheDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/mCOP/";
    }
}