// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.domain;

import com.microsoft.azure.spring.data.cosmosdb.core.mapping.Document;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@Document()
public class PageableAddress {
    @Id
    private String postalCode;
    private String street;
    @PartitionKey
    private String city;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PageableAddress address = (PageableAddress) o;
        return Objects.equals(postalCode, address.postalCode)
            && Objects.equals(street, address.street)
            && Objects.equals(city, address.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postalCode, street, city);
    }

    public PageableAddress() {
    }

    public PageableAddress(String postalCode, String street, String city) {
        this.postalCode = postalCode;
        this.street = street;
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "PageableAddress{"
            + "postalCode='"
            + postalCode
            + '\''
            + ", street='"
            + street
            + '\''
            + ", city='"
            + city
            + '\''
            + '}';
    }
}
