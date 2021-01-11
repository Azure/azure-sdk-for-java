// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common.domain;

import com.azure.spring.data.gremlin.annotation.Vertex;
import org.springframework.data.annotation.Id;

@Vertex
public class Book {

    @Id
    private Integer serialNumber;

    private String name;

    private Double price;

    public Book() {
    }

    public Book(Integer serialNumber, String name, Double price) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.price = price;
    }

    public Integer getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(Integer serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
