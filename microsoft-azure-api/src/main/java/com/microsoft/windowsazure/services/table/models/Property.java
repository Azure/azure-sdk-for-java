package com.microsoft.windowsazure.services.table.models;

public class Property {
    private String edmType;
    private Object value;

    public String getEdmType() {
        return edmType;
    }

    public Property setEdmType(String edmType) {
        this.edmType = edmType;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public Property setValue(Object value) {
        this.value = value;
        return this;
    }
}