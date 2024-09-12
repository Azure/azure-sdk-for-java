// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

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
     * @param top Maximum number of responses that will be returned. Default: 5, minimum: 1 and maximum: 20.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setTop(Integer top) {
        this.top = top;
        return this;
    }

    /**
     *
     * @return query
     */
    public String getQuery() {
        return query;
    }

    /**
     *
     * @param query A string that contains information about a location, such as an address or landmark name.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setQuery(String query) {
        this.query = query;
        return this;
    }

    /**
     *
     * @return addressLine
     */
    public String getAddressLine() {
        return addressLine;
    }

    /**
     *
     * @param addressLine The official street line of an address relative to the area, as specified by the locality, or
     * postalCode, properties. Typical use of this element would be to provide a street address or any official address.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setAddressLine(String addressLine) {
        this.addressLine = addressLine;
        return this;
    }

    /**
     *
     * @return view
     */
    public String getView() {
        return view;
    }

    /**
     *
     * @param view A string that represents an [ISO 3166-1 Alpha-2 region/country
     * code](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2). This will alter Geopolitical disputed borders and labels
     * to align with the specified user region. By default, the View parameter is set to “Auto” even if you haven’t
     * defined it in the request.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setView(String view) {
        this.view = view;
        return this;
    }

    /**
     *
     * @param boundingBox A rectangular area on the earth defined as a bounding box object. The sides of the rectangles are
     * defined by longitude and latitude values. When you specify this parameter, the geographical area is taken into
     * account when computing the results of a location query.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setBoundingBox(GeoBoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        return this;
    }

    /**
     *
     * @return countryRegion
     */
    public String getCountryRegion() {
        return countryRegion;
    }

    /**
     *
     * @param countryRegion Signal for the geocoding result to an [ISO 3166-1 Alpha-2 region/country
     * code](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2) that is specified e.g. FR./
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setCountryRegion(String countryRegion) {
        this.countryRegion = countryRegion;
        return this;
    }

    /**
     *
     * @return adminDistrict2
     */
    public String getAdminDistrict2() {
        return adminDistrict2;
    }

    /**
     *
     * @param adminDistrict2 The county for the structured address, such as King.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setAdminDistrict2(String adminDistrict2) {
        this.adminDistrict2 = adminDistrict2;
        return this;
    }

    /**
     *
     * @return locality
     */
    public String getLocality() {
        return locality;
    }

    /**
     *
     * @param locality The locality portion of an address, such as Seattle.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setLocality(String locality) {
        this.locality = locality;
        return this;
    }

    /**
     *
     * @return postalCode
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     *
     * @param postalCode The postal code portion of an address.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    /**
     *
     * @return adminDistrict3
     */
    public String getAdminDistrict3() {
        return adminDistrict3;
    }

    /**
     *
     * @param adminDistrict3 The named area for the structured address.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setAdminDistrict3(String adminDistrict3) {
        this.adminDistrict3 = adminDistrict3;
        return this;
    }

    /**
     *
     * @return adminDistrict
     */
    public String getAdminDistrict() {
        return adminDistrict;
    }

    /**
     *
     * @param adminDistrict The country subdivision portion of an address, such as WA.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setAdminDistrict(String adminDistrict) {
        this.adminDistrict = adminDistrict;
        return this;
    }

    /**
     *
     * @return coordinates
     */
    public GeoPosition getCoordinates() {
        return coordinates;
    }

    /**
     *
     * @param coordinates A point on the earth specified as a longitude and latitude. When you specify this parameter,
     * the user’s location is taken into account and the results returned may be more relevant to the user. Example:
     * &amp;coordinates=lon,lat.
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
     * Return GeoBoundingBox
     * @return {@link Optional<GeoBoundingBox>}
     */
    public Optional<GeoBoundingBox> getBoundingBox() {
        return Optional.ofNullable(boundingBox);
    }

}
