package com.atakmap.android.plugintemplate.araaftorPlugin;

import com.atakmap.android.plugintemplate.detection.BoundingBox;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DetectObject {
    public int id;
    public String name;
    public Date timestamp;
    public String imageName;
    public List<BoundingBox> result;
    public String photoPath;
    public String forceImageName;
    public List<Double> dist;
    public double logtitude;
    public double latitude;
    public double altitude;
    public double azimuth;
    public String UID;
    public double mean_dist;

    public DetectObject(int id, String name, Date timestamp, String imageName, List<BoundingBox> result, String photoPath, String forceImageName,   List<Double> dist, double logtitude, double latitude, double altitude, String UID, double mean_dist, double azimuth) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
        this.imageName = imageName;
        this.result = result;
        this.photoPath = photoPath;
        this.forceImageName = forceImageName;
        this.dist = dist;
        this.logtitude = logtitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.UID = UID;
        this.mean_dist = mean_dist;
        this.azimuth=azimuth;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAzimuth(double azimuth) {
        this.azimuth = azimuth;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMean_dist(double mean_dist) {
        this.mean_dist = mean_dist;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<BoundingBox> getResult() {
        return result;
    }

    public void setResult(BoundingBox[] result) {
        this.result = Arrays.asList(result);
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getForceImageName() {
        return forceImageName;
    }

    public void setForceImageName(String forceImageName) {
        this.forceImageName = forceImageName;
    }


    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setDist(double dist) {
        this.mean_dist = dist;
    }

    public void setLogtitude(double logtitude) {
        this.logtitude = logtitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getImageName() {
        return imageName;
    }


    public List<Double> getDist() {
        return dist;
    }

    public double getLogtitude() {
        return logtitude;
    }

    public double getLatitude() {
        return latitude;
    }

}
