// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.common.domain;

import com.microsoft.spring.data.gremlin.annotation.Vertex;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@Vertex
public class Book {

    @Id
    private String serialNumber;

    private String name;

    private Double price;

    public Book() {
    }

    public Book(String serialNumber, String name, Double price) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.price = price;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Book book = (Book) o;
        return Objects.equals(serialNumber, book.serialNumber)
            && Objects.equals(name, book.name)
            && Objects.equals(price, book.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serialNumber, name, price);
    }
}
