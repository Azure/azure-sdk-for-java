package com.azure.spring.cloud.feature.manager.testobjects;

public class DiscountBanner {

    private Integer size;

    private String color;

    public Integer getSize() {
        return size;
    }

    public DiscountBanner setSize(Integer size) {
        this.size = size;
        return this;
    }

    public String getColor() {
        return color;
    }

    public DiscountBanner setColor(String color) {
        this.color = color;
        return this;
    }
    
    @Override
    public String toString() {
        return "DiscountBannder: Size " + size + " Color " + color;
    }
}
