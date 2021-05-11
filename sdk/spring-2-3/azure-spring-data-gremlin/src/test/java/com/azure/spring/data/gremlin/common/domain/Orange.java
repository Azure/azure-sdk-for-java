// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common.domain;

import com.azure.spring.data.gremlin.annotation.GeneratedValue;
import com.azure.spring.data.gremlin.annotation.Vertex;
import org.springframework.data.annotation.Id;

@Vertex
public class Orange {

    @Id
    @GeneratedValue
    private String id;

    private String location;

    private Double price;

    public Orange(String location, Double price) {
        this.location = location;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
