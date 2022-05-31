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
     * @return
     */
    public Boolean includeSpeedLimit() {
        return includeSpeedLimit;
    }

    /**
     * Returns the building number.
     * @return
     */
    public String getNumber() {
        return number;
    }

    /**
     * Returns whether we should include the road use.
     * @return
     */
    public Boolean includeRoadUse() {
        return includeRoadUse;
    }

    /**
     * Returns a list of road uses.
     * @return
     */
    public List<RoadUseType> getRoadUse() {
        return roadUse;
    }

    /**
     * Returns whether to allow free form newline.
     * @return
     */
    public Boolean allowFreeformNewline() {
        return allowFreeformNewline;
    }

    /**
     * Returns whether to include match type.
     * @return
     */
    public Boolean includeMatchType() {
        return includeMatchType;
    }

    /**
     * Returns the entity type.
     * @return
     */
    public GeographicEntityType getEntityType() {
        return entityType;
    }

    /**
     * Sets whether to include the speed limit.
     * @param includeSpeedLimit
     * @return
     */
    public ReverseSearchAddressOptions setIncludeSpeedLimit(Boolean includeSpeedLimit) {
        this.includeSpeedLimit = true;
        return this;
    }

    /**
     * Sets whether to include the street number.
     * @param number
     * @return
     */
    public ReverseSearchAddressOptions setNumber(String number) {
        this.number = number;
        return this;
    }

    /**
     * Sets whether to include road use.
     * @param includeRoadUse
     * @return
     */
    public ReverseSearchAddressOptions setIncludeRoadUse(Boolean includeRoadUse) {
        this.includeRoadUse = includeRoadUse;
        return this;
    }

    /**
     * Sets a list of road use types.
     * @param roadUse
     * @return
     */
    public ReverseSearchAddressOptions setRoadUse(List<RoadUseType> roadUse) {
        this.roadUse = roadUse;
        return this;
    }

    /**
     * Sets whether to allow free form newline.
     * @param allowFreeformNewline
     * @return
     */
    public ReverseSearchAddressOptions setAllowFreeformNewline(Boolean allowFreeformNewline) {
        this.allowFreeformNewline = allowFreeformNewline;
        return this;
    }

    /**
     * Sets whether to include the match type.
     * @param includeMatchType
     * @return
     */
    public ReverseSearchAddressOptions setIncludeMatchType(Boolean includeMatchType) {
        this.includeMatchType = includeMatchType;
        return this;
    }

    /**
     * Sets the entity type.
     * @param entityType
     * @return
     */
    public ReverseSearchAddressOptions setEntityType(GeographicEntityType entityType) {
        this.entityType = entityType;
        return this;
    }
}
