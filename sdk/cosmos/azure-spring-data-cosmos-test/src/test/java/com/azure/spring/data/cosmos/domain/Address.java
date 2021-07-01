// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@Container()
public class Address {

    public static final Address TEST_ADDRESS1_PARTITION1 = new Address(
        TestConstants.POSTAL_CODE, TestConstants.STREET, TestConstants.CITY);
    public static final Address TEST_ADDRESS2_PARTITION1 = new Address(
        TestConstants.POSTAL_CODE_0, TestConstants.STREET_0, TestConstants.CITY);
    public static final Address TEST_ADDRESS1_PARTITION2 = new Address(
        TestConstants.POSTAL_CODE_1, TestConstants.STREET_1, TestConstants.CITY_0);
    public static final Address TEST_ADDRESS4_PARTITION3 = new Address(
        TestConstants.POSTAL_CODE, TestConstants.STREET_2, TestConstants.CITY_1);

    @Id
    String postalCode;
    String street;
    @PartitionKey
    String city;

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Address address = (Address) o;
        return Objects.equals(postalCode, address.postalCode)
            && Objects.equals(street, address.street)
            && Objects.equals(city, address.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postalCode, street, city);
    }

    @Override
    public String toString() {
        return "Address{"
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

    public Address(String postalCode, String street, String city) {
        this.postalCode = postalCode;
        this.street = street;
        this.city = city;
    }

    public Address() {
    }
}
