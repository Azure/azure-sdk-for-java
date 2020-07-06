// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.common.domain;

import com.microsoft.spring.data.gremlin.annotation.GeneratedValue;
import com.microsoft.spring.data.gremlin.annotation.Vertex;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@Vertex
public class Orange {

    @Id
    @GeneratedValue
    private String id;

    private String location;

    private Double price;

    public Orange() {
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Orange orange = (Orange) o;
        return Objects.equals(id, orange.id)
            && Objects.equals(location, orange.location)
            && Objects.equals(price, orange.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, location, price);
    }
}
