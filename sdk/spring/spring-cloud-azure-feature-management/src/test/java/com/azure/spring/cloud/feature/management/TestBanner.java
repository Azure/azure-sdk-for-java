package com.azure.spring.cloud.feature.management;

public class TestBanner {

    private Integer size;

    private String color;

    /**
     * @return the size
     */
    public Integer getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public TestBanner setSize(Integer size) {
        this.size = size;
        return this;
    }

    /**
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public TestBanner setColor(String color) {
        this.color = color;
        return this;
    }

}
