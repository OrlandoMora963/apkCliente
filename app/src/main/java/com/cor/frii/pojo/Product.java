package com.cor.frii.pojo;

public class Product {

    private int Id;
    private String Name;
    private String Description;
    private float Price;
    private int UnitMeasurement;
    private int Size;
    private String Url;
    private String type;
    private String marke;

    public Product(int id, String name, String description, float price, int unitMeasurement, int size, String url, String type, String marke) {
        Id = id;
        Name = name;
        Description = description;
        Price = price;
        UnitMeasurement = unitMeasurement;
        Size = size;
        Url = url;

        this.type = type;
        this.marke = marke;
    }

    public String getType() {
        return type;
    }

    public String getMarke() {
        return marke;
    }

    public void setMarke(String marke) {
        this.marke = marke;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public float getPrice() {
        return Price;
    }

    public void setPrice(float price) {
        Price = price;
    }

    public int getUnitMeasurement() {
        return UnitMeasurement;
    }

    public void setUnitMeasurement(int unitMeasurement) {
        UnitMeasurement = unitMeasurement;
    }

    public int getSize() {
        return Size;
    }

    public void setSize(int size) {
        Size = size;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }
}
