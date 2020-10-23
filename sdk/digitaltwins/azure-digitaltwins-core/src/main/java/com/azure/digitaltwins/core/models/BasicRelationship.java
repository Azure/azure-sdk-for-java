// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

/**
 * Although relationships have a user-defined schema, these properties should exist on every instance.
 * This is useful to use as a base class to ensure your custom relationships have the necessary properties.
 */
@Fluent
@JsonInclude(Include.NON_NULL)
public final class BasicRelationship {

    @JsonProperty(value = "$relationshipId", required = true)
    private String id;

    @JsonProperty(value = "$sourceId", required = true)
    private String sourceId;

    @JsonProperty(value = "$targetId", required = true)
    private String targetId;

    @JsonProperty(value = "$relationshipName", required = true)
    private String name;

    @JsonIgnore
    private final Map<String, Object> customProperties = new HashMap<>();

    /**
     * Construct a basic digital twin relationship.
     * @param relationshipId The unique Id of this relationship.
     * @param sourceDigitalTwinId The digital twin that this relationship comes from.
     * @param targetDigitalTwinId The digital twin that this relationship points to.
     * @param relationshipName The user defined name of this relationship, for instance "Contains" or "isAdjacentTo"
     */
    public BasicRelationship(
        String relationshipId,
        String sourceDigitalTwinId,
        String targetDigitalTwinId,
        String relationshipName) {
        this.id = relationshipId;
        this.sourceId = sourceDigitalTwinId;
        this.targetId = targetDigitalTwinId;
        this.name = relationshipName;
    }

    // Empty constructor for json deserialization purposes
    private BasicRelationship() {
    }

    /**
     * Gets the unique Id of the relationship. This field is present on every relationship.
     * @return The unique Id of the relationship. This field is present on every relationship.
     */
    public String getRelationshipId() {
        return id;
    }

    /**
     * Gets the unique Id of the source digital twin. This field is present on every relationship.
     * @return The unique Id of the source digital twin. This field is present on every relationship.
     */
    public String getSourceDigitalTwinId() {
        return sourceId;
    }

    /**
     * Gets the unique Id of the target digital twin. This field is present on every relationship.
     * @return The unique Id of the target digital twin. This field is present on every relationship.
     */
    public String getTargetDigitalTwinId() {
        return targetId;
    }

    /**
     * Gets the name of the relationship, which defines the type of link (e.g. Contains). This field is present on every relationship.
     * @return The name of the relationship, which defines the type of link (e.g. Contains). This field is present on every relationship.
     */
    public String getRelationshipName() {
        return name;
    }

    /**
     * Gets the additional properties defined in the model. This field will contain any properties of the relationship that are not already defined by the other strong types of this class.
     * @return The additional properties defined in the model. This field will contain any properties of the relationship that are not already defined by the other strong types of this class.
     */
    @JsonAnyGetter
    public Map<String, Object> getCustomProperties() {
        return customProperties;
    }

    /**
     * Adds an additional property to this model. This field will contain any properties of the relationship that are not already defined by the other strong types of this class.
     * @param key The key of the additional property to be added to the relationship.
     * @param value The value of the additional property to be added to the relationship.
     * @return The BasicRelationship object itself.
     */
    @JsonAnySetter
    public BasicRelationship addCustomProperty(String key, Object value) {
        this.customProperties.put(key, value);
        return this;
    }

}
