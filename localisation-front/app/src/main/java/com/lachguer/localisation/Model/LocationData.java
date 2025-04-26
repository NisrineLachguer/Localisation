package com.lachguer.localisation.Model;

import java.util.Date;

public class LocationData {
    private double latitude;
    private double longitude;
    private Date timestamp;
    private String imei;
    private float accuracy;

    // Constructor, getters and setters
    public LocationData(double latitude, double longitude, Date timestamp, String imei, float accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.imei = imei;
        this.accuracy = accuracy;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getImei() {
        return imei;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }
}