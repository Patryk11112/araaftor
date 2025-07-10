package com.atakmap.android.plugintemplate.araaftorPlugin;


import java.text.DecimalFormat;

public class CalculationCords {

    public String calculateCordinats(double x, String name){
        DecimalFormat dec = new DecimalFormat("0.00000");
        if(name.equals("DM")){
            return decimalToDM(x);
        } else if (name.equals("DMS")) {
            return decimalToDMS(x);
        } else if (name.equals("DD")) {
            return String.valueOf(dec.format(x));
        }
        return name;
    }
    public static String decimalToDM(double decimalDegree) {
        int degree = (int) decimalDegree;

        double minutesDecimal = (decimalDegree - degree) * 60;
        int minutes = (int) minutesDecimal;
        double seconds = (minutesDecimal - minutes) * 60;

        String direction = "";
        if (degree < 0) {
            degree = -degree;
            if (decimalDegree < 0) {
                direction = (decimalDegree == degree) ? "S" : "W";
            }
        } else {
            if (decimalDegree < 0) {
                direction = (decimalDegree == degree) ? "N" : "E";
            }
        }
        return degree + "° " + minutes + "' " + String.format("%.2f", seconds) + "\" " + direction;

    }

    public static String decimalToDMS(double decimalDegree) {
        int degree = (int) decimalDegree;

        double minutesDecimal = (decimalDegree - degree) * 60;
        int minutes = (int) minutesDecimal;

        double secondsDecimal = (minutesDecimal - minutes) * 60;
        int seconds = (int) secondsDecimal;
        double fractionalSeconds = secondsDecimal - seconds;

        String direction = "";
        if (degree < 0) {
            degree = -degree;
            if (decimalDegree < 0) {
                direction = (decimalDegree == degree) ? "S" : "W";
            }
        } else {
            if (decimalDegree < 0) {
                direction = (decimalDegree == degree) ? "N" : "E";
            }
        }
        return degree + "° " + minutes + "' " + seconds + "\" " + String.format("%.2f", fractionalSeconds) + " " + direction;
    }
    double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
    public double calculateDistance(double startLat, double startLong, double endLat, double endLong) {
        final double R = 6371000;
        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

}
