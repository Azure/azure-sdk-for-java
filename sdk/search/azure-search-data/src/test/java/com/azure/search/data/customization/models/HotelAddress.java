// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class HotelAddress {
    @JsonProperty(value = "StreetAddress")
    private String streetAddress;

    @JsonProperty(value = "City")
    private String city;

    @JsonProperty(value = "StateProvince")
    private String stateProvince;

    @JsonProperty(value = "Country")
    private String country;

    @JsonProperty(value = "PostalCode")
    private String postalCode;


    public String streetAddress(){
        return this.streetAddress;
    }

    public HotelAddress streetAddress(String streetAddress){
        this.streetAddress = streetAddress;
        return this;
    }

    public String city(){
        return this.city;
    }

    public HotelAddress city(String city){
        this.city = city;
        return this;
    }

    public String stateProvince(){
        return this.stateProvince;
    }

    public HotelAddress stateProvince(String stateProvince){
        this.stateProvince = stateProvince;
        return this;
    }

    public String country(){
        return this.country;
    }

    public HotelAddress country(String country){
        this.country = country;
        return this;
    }

    public String postalCode(){
        return this.postalCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HotelAddress)) return false;
        HotelAddress that = (HotelAddress) o;
        return Objects.equals(streetAddress, that.streetAddress) &&
            Objects.equals(city, that.city) &&
            Objects.equals(stateProvince, that.stateProvince) &&
            Objects.equals(country, that.country) &&
            Objects.equals(postalCode, that.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streetAddress, city, stateProvince, country, postalCode);
    }

    public HotelAddress postalCode(String postalCode){
        this.postalCode = postalCode;
        return this;
    }
}
