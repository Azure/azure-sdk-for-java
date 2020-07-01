// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.domain;

import com.microsoft.azure.spring.data.cosmosdb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@Document
public class IntegerIdDomain {

    @Id
    private Integer number;

    private String name;

    public IntegerIdDomain(Integer number, String name) {
        this.number = number;
        this.name = name;
    }

    public IntegerIdDomain() {
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IntegerIdDomain that = (IntegerIdDomain) o;
        return Objects.equals(number, that.number)
            && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, name);
    }

    @Override
    public String toString() {
        return "IntegerIdDomain{"
            + "number="
            + number
            + ", name='"
            + name
            + '\''
            + '}';
    }
}
