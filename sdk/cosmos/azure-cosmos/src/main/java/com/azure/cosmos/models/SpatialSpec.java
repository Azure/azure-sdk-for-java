// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The type Spatial spec.
 */
public final class SpatialSpec {

    private List<SpatialType> spatialTypes;

    private JsonSerializable jsonSerializable;

    /**
     * Constructor.
     */
    public SpatialSpec() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the included path.
     */
    SpatialSpec(String jsonString) {
        this.jsonSerializable = new JsonSerializable(jsonString);
    }

    /**
     * Constructor.
     *
     * @param objectNode the object node that represents the included path.
     */
    SpatialSpec(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    /**
     * Gets path.
     *
     * @return the path.
     */
    public String getPath() {
        return this.jsonSerializable.getString(Constants.Properties.PATH);
    }

    /**
     * Sets path.
     *
     * @param path the path.
     * @return the SpatialSpec.
     */
    public SpatialSpec setPath(String path) {
        this.jsonSerializable.set(
            Constants.Properties.PATH,
            path,
            CosmosItemSerializer.DEFAULT_SERIALIZER);
        return this;
    }

    /**
     * Gets the container of spatial types.
     *
     * @return the container of spatial types.
     */
    public List<SpatialType> getSpatialTypes() {
        if (this.spatialTypes == null) {
            this.spatialTypes = this.jsonSerializable.getList(Constants.Properties.TYPES, SpatialType.class, true);

            if (this.spatialTypes == null) {
                this.spatialTypes = new ArrayList<SpatialType>();
            }
        }

        return this.spatialTypes;
    }

    /**
     * Sets the container of spatial types.
     *
     * @param spatialTypes the container of spatial types.
     * @return the SpatialSpec.
     */
    public SpatialSpec setSpatialTypes(List<SpatialType> spatialTypes) {
        this.spatialTypes = spatialTypes;
        Collection<String> spatialTypeNames = new ArrayList<String>();
        for (SpatialType spatialType : this.spatialTypes) {
            spatialTypeNames.add(spatialType.toString());
        }
        this.jsonSerializable.set(
            Constants.Properties.TYPES,
            spatialTypeNames,
            CosmosItemSerializer.DEFAULT_SERIALIZER);
        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }
}
