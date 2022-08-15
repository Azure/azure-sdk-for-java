// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

import java.util.List;

import com.azure.core.models.GeoPosition;

/**
 * Class holding optional parameters for Reverse Search.
 */
public final class ReverseSearchAddressOptions extends BaseReverseSearchOptions<ReverseSearchAddressOptions> {
    private Boolean includeSpeedLimit;
    private String number;
    private Boolean includeRoadUse;
    private List<RoadUseType> roadUse;
    private Boolean allowFreeformNewline;
    private Boolean includeMatchType;
    private GeographicEntityType entityType;

    /**
     * Builds fuzzy search options with query string and coordinates.
     * @param coordinates The {@link GeoPosition} coordinates to be searched.
     */
    public ReverseSearchAddressOptions(GeoPosition coordinates) {
        this.setCoordinates(coordinates);
    }

    /**
     * Returns whether we should include the speed limit.
     * @return whether we should include the speed limit.
     */
    public Boolean includeSpeedLimit() {
        return includeSpeedLimit;
    }

    /**
     * Returns the building number.
     * @return the building number.
     */
    public String getNumber() {
        return number;
    }

    /**
     * Returns whether we should include the road use.
     * @return whether we should include the road use.
     */
    public Boolean includeRoadUse() {
        return includeRoadUse;
    }

    /**
     * Returns a list of road uses.
     * @return a list of road uses.
     */
    public List<RoadUseType> getRoadUse() {
        return roadUse;
    }

    /**
     * Returns whether to allow free form newline.
     * @return whether to allow free form newline.
     */
    public Boolean allowFreeformNewline() {
        return allowFreeformNewline;
    }

    /**
     * Returns whether to include match type.
     * @return whether to include match type.
     */
    public Boolean includeMatchType() {
        return includeMatchType;
    }

    /**
     * Returns the entity type.
     * @return the entity type.
     */
    public GeographicEntityType getEntityType() {
        return entityType;
    }

    /**
     * Sets whether to include the speed limit.
     * @param includeSpeedLimit whether to include the speed limit.
     * @return a reference to this {@code ReverseSearchAddressOptions}.
     */
    public ReverseSearchAddressOptions setIncludeSpeedLimit(Boolean includeSpeedLimit) {
        this.includeSpeedLimit = true;
        return this;
    }

    /**
     * Sets the street number.
     * @param number the street number.
     * @return a reference to this {@code ReverseSearchAddressOptions}.
     */
    public ReverseSearchAddressOptions setNumber(String number) {
        this.number = number;
        return this;
    }

    /**
     * Sets whether to include road use.
     * @param includeRoadUse whether to include road use.
     * @return a reference to this {@code ReverseSearchAddressOptions}.
     */
    public ReverseSearchAddressOptions setIncludeRoadUse(Boolean includeRoadUse) {
        this.includeRoadUse = includeRoadUse;
        return this;
    }

    /**
     * Sets a list of road use types.
     * @param roadUse a list of road use types.
     * @return a reference to this {@code ReverseSearchAddressOptions}.
     */
    public ReverseSearchAddressOptions setRoadUse(List<RoadUseType> roadUse) {
        this.roadUse = roadUse;
        return this;
    }

    /**
     * Sets whether to allow free form newline.
     * @param allowFreeformNewline whether to allow free form newline.
     * @return a reference to this {@code ReverseSearchAddressOptions}.
     */
    public ReverseSearchAddressOptions setAllowFreeformNewline(Boolean allowFreeformNewline) {
        this.allowFreeformNewline = allowFreeformNewline;
        return this;
    }

    /**
     * Sets whether to include the match type.
     * @param includeMatchType whether to include the match type.
     * @return a reference to this {@code ReverseSearchAddressOptions}.
     */
    public ReverseSearchAddressOptions setIncludeMatchType(Boolean includeMatchType) {
        this.includeMatchType = includeMatchType;
        return this;
    }

    /**
     * Sets the entity type.
     * @param entityType the entity type.
     * @return a reference to this {@code ReverseSearchAddressOptions}.
     */
    public ReverseSearchAddressOptions setEntityType(GeographicEntityType entityType) {
        this.entityType = entityType;
        return this;
    }
}
