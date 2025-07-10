package com.atakmap.android.plugintemplate.araaftorPlugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class Util {
    public static final int FORMAT_DOUBLE = 1;
    public static final int FORMAT_DEG = 2;
    public static final int FORMAT_AR = 3;

    public static String trimToSize(String string, int length) {
        if (string.length() > length)
            string = string.substring(0, length);
        return string;
    }

    public static String[] getLatLonString(double[] latlon, int format) {
        String[] latitudelongitude = new String[2];
        switch (format) {
            case FORMAT_DOUBLE:
                latitudelongitude[0] = (latlon[0] > 0) ? (trimToSize(latlon[0] + "", 11) + "°N") :
                        (trimToSize(-latlon[0] + "", 11) + "°S");
                latitudelongitude[1] = (latlon[1] > 0) ? (trimToSize(latlon[1] + "", 11) + "°E") :
                        (trimToSize(-latlon[1] + "", 11) + "°W");
                break;
            case FORMAT_DEG:

                latitudelongitude[0] = (latlon[0] > 0) ? (trimToSize(doubleToDMS(latlon[0]) + "", 11) + "N") :
                        (trimToSize(doubleToDMS(-latlon[0]) + "", 11) + "S");
                latitudelongitude[1] = (latlon[1] > 0) ? (trimToSize(doubleToDMS(latlon[1]) + "", 11) + "E") :
                        (trimToSize(doubleToDMS(-latlon[1]) + "", 11) + "W");
                break;

            case FORMAT_AR:
                latitudelongitude = new String[4];

                latitudelongitude[0] = (latlon[0] > 0) ? (String.format("%.4f°N", latlon[0])) :
                        (String.format("%.4f°S", -latlon[0]));

                latitudelongitude[1] = (latlon[0] > 0) ? (doubleToDMS(latlon[0]) + "N") :
                        (doubleToDMS(-latlon[0]) + "S");
                latitudelongitude[2] = (latlon[1] > 0) ? (String.format("%.4f°E", latlon[1])) :
                        (String.format("%.4f°W", -latlon[1]));
                latitudelongitude[3] = (latlon[1] > 0) ? (doubleToDMS(latlon[1]) + "E") :
                        (doubleToDMS(-latlon[1]) + "W");


        }
        return latitudelongitude;

    }

    public static String doubleToDMS(double gpsDouble) {
        double[] data = new double[3];
        data[0] = Math.floor(gpsDouble);
        data[2] = (gpsDouble - data[0]) * 3600.0;
        data[1] = Math.floor(data[2] / 60.0);
        data[2] = Math.floor(data[2] - (data[1] * 60.0));
        String dms = "";
        String[] ending = {"°", "'", "\""};
        for (int i = 0; i < 3; i++) {
            dms += makeTwoDigit((int) data[i]) + ending[i];
        }

        return dms;

    }

    public static String makeTwoDigit(int digit) {
        String returned = "";
        if (digit < 10) {
            returned += "0";
        }
        returned += digit;
        return returned;
    }

    public static int getRankLevelFromApp6aCode(String string) {
        string = string.toUpperCase();
        char rank = string.charAt(11);
        Log.d("SAME-util", "selected char: " + rank);
        Log.d("SAME-util", "For " + string + " goooot " + rank + "- A = " + (int) rank + " - " + (int) 'A');
        return (int) rank - (int) 'A';
    }

    public static Rect locateView(View view) {
        Rect loc = new Rect();
        int[] location = new int[2];
        if (view == null) {
            return loc;
        }
        view.getLocationOnScreen(location);

        loc.left = location[0];
        loc.top = location[1];
        loc.right = loc.left + view.getWidth();
        loc.bottom = loc.top + view.getHeight();
        return loc;
    }

    public static String degToThousandth(float azimuth) {
        int thousandth = (int) Math.round(azimuth / 0.05625);
        int partOne = thousandth / 100;
        String partOneS = (partOne >= 10) ? "" : "0";
        partOneS += partOne;

        int partTwo = thousandth % 100;
        String partTwoS = (partTwo >= 10) ? "" : "0";
        partTwoS += partTwo;

        return partOneS + "-" + partTwoS;
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%');
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
        return "";
    }

    public static void vibrate(Context context, int interval) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(interval);
    }

    public static String getHost(Context context, boolean isCommander) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        //dół
        final String defHost = "10.1.1.47";
        //pokój
//        final String host = "tcp://10.128.134.214:1883";
        //ogólnie
        final String host = "tcp://" + (settings.getString("pref_commander_ip", defHost)) + ":1883";
        return host;
    }
}