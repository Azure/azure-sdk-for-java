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

    /**
     *
     * @param top
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setTop(Integer top) {
        this.top = top;
        return this;
    }

    /**
     *
     * @return {@link BaseSearchOptions}
     */
    public String getQuery() {
        return query;
    }

    /**
     *
     * @param query
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setQuery(String query) {
        this.query = query;
        return this;
    }

    /**
     *
     * @return {@link BaseSearchOptions}
     */
    public String getAddressLine() {
        return addressLine;
    }

    /**
     *
     * @param addressLine
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setAddressLine(String addressLine) {
        this.addressLine = addressLine;
        return this;
    }

    /**
     *
     * @return {@link BaseSearchOptions}
     */
    public String getView() {
        return view;
    }

    /**
     *
     * @param view
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setView(String view) {
        this.view = view;
        return this;
    }

    /**
     *
     * @param boundingBox
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setBoundingBox(GeoBoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        return this;
    }

    /**
     *
     * @return {@link BaseSearchOptions}
     */
    public String getCountryRegion() {
        return countryRegion;
    }

    /**
     *
     * @param countryRegion
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setCountryRegion(String countryRegion) {
        this.countryRegion = countryRegion;
        return this;
    }

    /**
     *
     * @return {@link BaseSearchOptions}
     */
    public String getAdminDistrict2() {
        return adminDistrict2;
    }

    /**
     *
     * @param adminDistrict2
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setAdminDistrict2(String adminDistrict2) {
        this.adminDistrict2 = adminDistrict2;
        return this;
    }

    /**
     *
     * @return {@link BaseSearchOptions}
     */
    public String getLocality() {
        return locality;
    }

    /**
     *
     * @param locality
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setLocality(String locality) {
        this.locality = locality;
        return this;
    }

    /**
     *
     * @return {@link BaseSearchOptions}
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     *
     * @param postalCode
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    /**
     *
     * @return {@link BaseSearchOptions}
     */
    public String getAdminDistrict3() {
        return adminDistrict3;
    }

    /**
     *
     * @param adminDistrict3
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setAdminDistrict3(String adminDistrict3) {
        this.adminDistrict3 = adminDistrict3;
        return this;
    }

    /**
     *
     * @return {@link BaseSearchOptions}
     */
    public String getAdminDistrict() {
        return adminDistrict;
    }

    /**
     *
     * @param adminDistrict
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setAdminDistrict(String adminDistrict) {
        this.adminDistrict = adminDistrict;
        return this;
    }

    /**
     *
     * @return {@link BaseSearchOptions}
     */
    public GeoPosition getCoordinates() {
        return coordinates;
    }

    /**
     *
     * @param coordinates
     * @return {@link BaseSearchOptions}
     */
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

    /**
     *
     * @return {@link Optional<GeoBoundingBox>}
     */
    public Optional<GeoBoundingBox> getBoundingBox() {
        return Optional.ofNullable(boundingBox);
    }

}
