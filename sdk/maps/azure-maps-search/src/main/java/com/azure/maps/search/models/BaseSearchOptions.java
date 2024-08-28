// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

import java.util.List;
import java.util.Optional;

import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoPosition;

/**
 * Class holding optional parameters for Search.
 *
 */

public  class BaseSearchOptions {
    private Integer top;
    private String query;
    private String addressLine;
    private String countryRegion;
    private GeoBoundingBox boundingBox;
    private String view;
    private GeoPosition coordinates;
    private String adminDistrict;
    private String adminDistrict2;
    private String adminDistrict3;
    private String locality;
    private String postalCode;

    /**
     * Creates a new instance of {@link BaseSearchOptions}.
     */
    public BaseSearchOptions() {
    }

    public BaseSearchOptions setTop(Integer top) {
        this.top = top;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public BaseSearchOptions setQuery(String query) {
        this.query = query;
        return this;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public BaseSearchOptions setAddressLine(String addressLine) {
        this.addressLine = addressLine;
        return this;
    }

    public String getView() {
        return view;
    }

    public BaseSearchOptions setView(String view) {
        this.view = view;
        return this;
    }

    public BaseSearchOptions setBoundingBox(GeoBoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        return this;
    }

    public String getCountryRegion() {
        return countryRegion;
    }

    public BaseSearchOptions setCountryRegion(String countryRegion) {
        this.countryRegion = countryRegion;
        return this;
    }

    public String getAdminDistrict2() {
        return adminDistrict2;
    }

    public BaseSearchOptions setAdminDistrict2(String adminDistrict2) {
        this.adminDistrict2 = adminDistrict2;
        return this;
    }

    public String getLocality() {
        return locality;
    }

    public BaseSearchOptions setLocality(String locality) {
        this.locality = locality;
        return this;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public BaseSearchOptions setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    public String getAdminDistrict3() {
        return adminDistrict3;
    }

    public BaseSearchOptions setAdminDistrict3(String adminDistrict3) {
        this.adminDistrict3 = adminDistrict3;
        return this;
    }

    public String getAdminDistrict() {
        return adminDistrict;
    }

    public BaseSearchOptions setAdminDistrict(String adminDistrict) {
        this.adminDistrict = adminDistrict;
        return this;
    }

    public GeoPosition getCoordinates() {
        return coordinates;
    }

    public BaseSearchOptions setCoordinates(GeoPosition coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    /**
     * Returns the top value.
     * @return the top value.
     */
    public Integer getTop() {
        return top;
    }

    public Optional<GeoBoundingBox> getBoundingBox() {
        return Optional.ofNullable(boundingBox);
    }

}
