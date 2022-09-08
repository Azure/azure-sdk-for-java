// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain.inheritance;

import org.springframework.data.annotation.Id;

import java.util.Objects;

public class Square extends Shape {
    @Id
    private String id;

    private int length;

    public Square(String id, int length, int area) {
        this.id = id;
        this.length = length;
        this.area = area;
    }

    public Square() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Square square = (Square) o;
        return length == square.length
            && Objects.equals(id, square.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, length);
    }

    @Override
    public String toString() {
        return "Square{"
            + "id='"
            + id
            + '\''
            + ", length="
            + length
            + '}';
    }
}
