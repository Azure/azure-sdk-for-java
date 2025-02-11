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

public class BaseSearchOptions {
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
     * Sets the maximum number of responses that will be returned for a search query, enabling control over the response size.
     * @param top Maximum number of responses that will be returned. Default: 5, minimum: 1 and maximum: 20.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setTop(Integer top) {
        this.top = top;
        return this;
    }

    /**
     * Retrieves the query string representing a location, such as an address or landmark.
     * @return query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query string used to specify a location for the search request.
     * @param query A string that contains information about a location, such as an address or landmark name.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setQuery(String query) {
        this.query = query;
        return this;
    }

    /**
     * Retrieves the address line associated with the search options.
     * @return addressLine
     */
    public String getAddressLine() {
        return addressLine;
    }

    /**
     * Sets the street address line for a more specific location in the search query.
     * @param addressLine The official street line of an address relative to the area, as specified by the locality, or
     * postalCode, properties. Typical use of this element would be to provide a street address or any official address.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setAddressLine(String addressLine) {
        this.addressLine = addressLine;
        return this;
    }

    /**
     * Retrieves the region-specific view parameter that affects geopolitical labeling.
     * @return view
     */
    public String getView() {
        return view;
    }

    /**
     * Sets the view parameter to align geopolitical borders and labels with a specific region.
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
     * Sets a rectangular geographical area for focusing the search within defined boundaries.
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
     * Retrieves the country or region code used to filter search results.
     * @return countryRegion
     */
    public String getCountryRegion() {
        return countryRegion;
    }

    /**
     * Sets the country or region code to narrow down the search results.
     * @param countryRegion Signal for the geocoding result to an [ISO 3166-1 Alpha-2 region/country
     * code](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2) that is specified e.g. FR./
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setCountryRegion(String countryRegion) {
        this.countryRegion = countryRegion;
        return this;
    }

    /**
     * Retrieves the secondary administrative district for the search options.
     * @return adminDistrict2
     */
    public String getAdminDistrict2() {
        return adminDistrict2;
    }

    /**
     * Sets the secondary administrative district to refine the search query.
     * @param adminDistrict2 The county for the structured address, such as King.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setAdminDistrict2(String adminDistrict2) {
        this.adminDistrict2 = adminDistrict2;
        return this;
    }

    /**
     * Retrieves the locality or city specified in the search options.
     * @return locality
     */
    public String getLocality() {
        return locality;
    }

    /**
     * Sets the locality or city to narrow the search results to a specific area.
     * @param locality The locality portion of an address, such as Seattle.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setLocality(String locality) {
        this.locality = locality;
        return this;
    }

    /**
     * Retrieves the postal code used in the search query.
     * @return postalCode
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the postal code for a more targeted search result.
     * @param postalCode The postal code portion of an address.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    /**
     * Retrieves the third-level administrative district for the search query.
     * @return adminDistrict3
     */
    public String getAdminDistrict3() {
        return adminDistrict3;
    }

    /**
     * Sets the third-level administrative district to refine the search.
     * @param adminDistrict3 The named area for the structured address.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setAdminDistrict3(String adminDistrict3) {
        this.adminDistrict3 = adminDistrict3;
        return this;
    }

    /**
     * Retrieves the first-level administrative district (e.g., a state or province) for the search options.
     * @return adminDistrict
     */
    public String getAdminDistrict() {
        return adminDistrict;
    }

    /**
     * Sets the first-level administrative district to refine the search query.
     * @param adminDistrict The country subdivision portion of an address, such as WA.
     * @return {@link BaseSearchOptions}
     */
    public BaseSearchOptions setAdminDistrict(String adminDistrict) {
        this.adminDistrict = adminDistrict;
        return this;
    }

    /**
     * Retrieves the geographic coordinates (latitude and longitude) used for location-based search relevance.
     * @return coordinates
     */
    public GeoPosition getCoordinates() {
        return coordinates;
    }

    /**
     * Sets the geographic coordinates to enhance the relevance of search results based on location.
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
     * Retrieves the maximum number of search responses allowed.
     * Returns the top value.
     * @return the top value.
     */
    public Integer getTop() {
        return top;
    }

    /**
     * Retrieves the geographical bounding box defined for the search.
     * Returns the GeoBoundingBox.
     * @return GeoBoundingBox
     */
    public Optional<GeoBoundingBox> getBoundingBox() {
        return Optional.ofNullable(boundingBox);
    }

}
