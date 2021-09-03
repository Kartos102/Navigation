package com.junjunguo.phialmaps.model;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on August 30, 2015.
 */
public class Place {
    private String name;
    private String address;
    private Double lat;
    private Double lng;

    public Place(String name, String address, Double lat, Double lng) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Double getLat() {
        return  lat;
    }
    public Double getLng() {
        return  lng;
    }
}
