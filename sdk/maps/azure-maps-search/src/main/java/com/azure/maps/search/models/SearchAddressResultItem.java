// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.maps.search.models;

import java.util.List;
import java.util.stream.Collectors;

import com.azure.core.annotation.Immutable;
import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoPosition;
import com.azure.maps.search.implementation.helpers.SearchAddressResultItemPropertiesHelper;
import com.azure.maps.search.implementation.helpers.Utility;
import com.azure.maps.search.implementation.models.BoundingBoxPrivate;
import com.azure.maps.search.implementation.models.EntryPointPrivate;
import com.azure.maps.search.implementation.models.SearchAddressResultItemPrivate;
import com.fasterxml.jackson.annotation.JsonIgnore;

/** Result object for a Search API response. */
@Immutable
public final class SearchAddressResultItem {
    private SearchAddressResultType type;
    private String id;
    private Double score;
    private Double distanceInMeters;
    private String info;
    private GeographicEntityType entityType;
    private PointOfInterest pointOfInterest;
    private Address address;
    @JsonIgnore
    private GeoPosition position;
    @JsonIgnore
    private GeoBoundingBox viewport;
    private List<EntryPoint> entryPoints;
    private AddressRanges addressRanges;
    private DataSource dataSource;
    private MatchType matchType;
    private Integer detourTime;

    static {
        SearchAddressResultItemPropertiesHelper.setAccessor(new SearchAddressResultItemPropertiesHelper
            .SearchAddressResultItemAccessor() {
            @Override
            public void setFromSearchAddressResultItemPrivate(SearchAddressResultItem resultItem,
                SearchAddressResultItemPrivate privateResultItem) {
                resultItem.setFromPrivateResultItem(privateResultItem);
            }
        });
    }

    /**
     * Get the type property: One of: * POI * Street * Geography * Point Address * Address Range * Cross Street.
     *
     * @return the type value.
     */
    public SearchAddressResultType getType() {
        return this.type;
    }

    /**
     * Get the id property: Id property.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the score property: The value within a result set to indicate the relative matching score between results.
     * You can use this to determine that result x is twice as likely to be as relevant as result y if the value of x is
     * 2x the value of y. The values vary between queries and is only meant as a relative value for one result set.
     *
     * @return the score value.
     */
    public Double getScore() {
        return this.score;
    }

    /**
     * Get the distanceInMeters property: Straight line distance between the result and geobias location in meters.
     *
     * @return the distanceInMeters value.
     */
    public Double getDistanceInMeters() {
        return this.distanceInMeters;
    }

    /**
     * Get the info property: Information about the original data source of the Result. Used for support requests.
     *
     * @return the info value.
     */
    public String getInfo() {
        return this.info;
    }

    /**
     * Get the entityType property: The entityType property.
     *
     * @return the entityType value.
     */
    public GeographicEntityType getEntityType() {
        return this.entityType;
    }

    /**
     * Get the pointOfInterest property: Details of the returned POI including information such as the name, phone, url
     * address, and classifications.
     *
     * @return the pointOfInterest value.
     */
    public PointOfInterest getPointOfInterest() {
        return this.pointOfInterest;
    }

    /**
     * Get the address property: The address of the result.
     *
     * @return the address value.
     */
    public Address getAddress() {
        return this.address;
    }

    /**
     * Get the position property: A location represented as a latitude and longitude using short names 'lat' &amp;
     * 'lon'.
     *
     * @return the position value.
     */
    public GeoPosition getPosition() {
        return this.position;
    }

    /**
     * Get the bounding box property: The bounding box that covers the result represented by the top-left and bottom-right
     * coordinates of the bounding box.
     *
     * @return the viewport value.
     */
    public GeoBoundingBox getBoundingBox() {
        return this.viewport;
    }

    /**
     * Get the entryPoints property: Array of EntryPoints. Those describe the types of entrances available at the
     * location. The type can be "main" for main entrances such as a front door, or a lobby, and "minor", for side and
     * back doors.
     *
     * @return the entryPoints value.
     */
    public List<EntryPoint> getEntryPoints() {
        return this.entryPoints;
    }

    /**
     * Get the addressRanges property: Describes the address range on both sides of the street for a search result.
     * Coordinates for the start and end locations of the address range are included.
     *
     * @return the addressRanges value.
     */
    public AddressRanges getAddressRanges() {
        return this.addressRanges;
    }

    /**
     * Get the dataSources property: Optional section. Reference geometry id for use with the [Get Search
     * Polygon](https://docs.microsoft.com/rest/api/maps/search/getsearchpolygon) API.
     *
     * @return the dataSources value.
     */
    public DataSource getDataSource() {
        return this.dataSource;
    }

    /**
     * Get the matchType property: Information on the type of match.
     *
     * <p>One of: * AddressPoint * HouseNumberRange * Street.
     *
     * @return the matchType value.
     */
    public MatchType getMatchType() {
        return this.matchType;
    }

    /**
     * Get the detourTime property: Detour time in seconds. Only returned for calls to the Search Along Route API.
     *
     * @return the detourTime value.
     */
    public Integer getDetourTime() {
        return this.detourTime;
    }

    // private setter
    private void setFromPrivateResultItem(SearchAddressResultItemPrivate privateResultItem) {
        this.type = privateResultItem.getType();
        this.id = privateResultItem.getId();
        this.score = privateResultItem.getScore();
        this.distanceInMeters = privateResultItem.getDistanceInMeters();
        this.info = privateResultItem.getInfo();
        this.entityType = privateResultItem.getEntityType();
        this.pointOfInterest = privateResultItem.getPointOfInterest();
        this.address = Utility.toAddress(privateResultItem.getAddress());
        this.position = new GeoPosition(privateResultItem.getPosition().getLon(),
            privateResultItem.getPosition().getLat());
        this.dataSource = privateResultItem.getDataSources();
        this.matchType = privateResultItem.getMatchType();
        this.detourTime = privateResultItem.getDetourTime();

        // convert bounding box
        BoundingBoxPrivate viewport = privateResultItem.getViewport();
        final double west = viewport.getTopLeft().getLon();
        final double north = viewport.getTopLeft().getLat();
        final double east = viewport.getBottomRight().getLon();
        final double south = viewport.getBottomRight().getLat();
        this.viewport = new GeoBoundingBox(west, south, east, north);

        // convert address ranges
        if (privateResultItem.getAddressRanges() != null) {
            this.addressRanges = Utility.toAddressRanges(privateResultItem.getAddressRanges());
        }

        // convert entry points
        List<EntryPointPrivate> privateEntryPoints = privateResultItem.getEntryPoints();

        if (privateEntryPoints != null) {
            this.entryPoints = privateEntryPoints.stream()
                .map(item -> Utility.toEntryPoint(item)).collect(Collectors.toList());
        }
    }
}
