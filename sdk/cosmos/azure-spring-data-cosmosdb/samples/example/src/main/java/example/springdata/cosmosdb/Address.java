// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package example.springdata.cosmosdb;

import org.springframework.data.annotation.Id;

public class Address {

    @Id
    private String postalCode;

    private String street;

    private String city;

    @Override
    public String toString() {
        return String.format("%s, %s, %s", this.street, this.city, this.postalCode);
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

    public Address(String postalCode, String street, String city) {
        this.postalCode = postalCode;
        this.street = street;
        this.city = city;
    }

    public Address() {
    }
}
