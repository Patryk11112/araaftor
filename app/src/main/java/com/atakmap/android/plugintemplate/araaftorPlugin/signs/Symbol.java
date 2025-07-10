package com.atakmap.android.plugintemplate.araaftorPlugin.signs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.ksoap2.serialization.SoapObject;


public class Symbol {
    private String code = null;
    private Bitmap icon = null;

    public Symbol(String code, Bitmap bitmap) {
        this.code = code;
        icon = bitmap;
    }

    public Symbol(String code, SoapObject response) {
        if (response == null) {
            Log.d("SAME", "Could not get app6a symbol");
        }

        this.code = code;

        try {
            String b64 = response.getPropertyAsString(0);
            byte[] byteArray = Base64.decode(b64, Base64.DEFAULT);
            icon = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        } catch (IllegalArgumentException e) {
            Log.e("SAME", "Could not parse the image");
        }
    }

    public Bitmap getIcon() {
        return icon;
    }

    public String getCode() {
        return code;
    }
}
