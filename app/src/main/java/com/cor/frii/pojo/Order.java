package com.cor.frii.pojo;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Order {

    private int id;
    private String date;
    private String status;
    private String phone;
    private String companyName;
    private LatLng companyDirection;
    private LatLng clientDirection;
    private List<String> detalles;
    private float calification;
    private long time;


    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public float getCalification() {
        return calification;
    }

    public void setCalification(float calification) {
        this.calification = calification;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getDetalles() {
        return detalles;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LatLng getCompanyDirection() {
        return companyDirection;
    }

    public void setCompanyDirection(LatLng companyDirection) {
        this.companyDirection = companyDirection;
    }

    public LatLng getClientDirection() {
        return clientDirection;
    }

    public void setClientDirection(LatLng clientDirection) {
        this.clientDirection = clientDirection;
    }

    public void setDetalles(List<String> detalles) {
        this.detalles = detalles;
    }
}
