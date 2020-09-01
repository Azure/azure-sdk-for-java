// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.implementation.serialization;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

/**
 * Although relationships have a user-defined schema, these properties should exist on every instance.
 * This is useful to use as a base class to ensure your custom relationships have the necessary properties.
 */
@Fluent
@JsonInclude(Include.NON_NULL)
public class BasicRelationship {

    @JsonProperty(value = "$relationshipId", required = true)
    private String id;

    @JsonProperty(value = "$sourceId", required = true)
    private String sourceId;

    @JsonProperty(value = "$targetId", required = true)
    private String targetId;

    @JsonProperty(value = "$relationshipName", required = true)
    private String name;

    private final Map<String, Object> customProperties = new HashMap<>();

    /**
     * Gets the unique Id of the relationship. This field is present on every relationship.
     * @return The unique Id of the relationship. This field is present on every relationship.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique Id of the relationship. This field is present on every relationship.
     * @param id The unique Id of the relationship. This field is present on every relationship.
     * @return The BasicRelationship object itself.
     */
    public BasicRelationship setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the unique Id of the source digital twin. This field is present on every relationship.
     * @return The unique Id of the source digital twin. This field is present on every relationship.
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * Sets the unique Id of the source digital twin. This field is present on every relationship.
     * @param sourceId The unique Id of the source digital twin. This field is present on every relationship.
     * @return The BasicRelationship object itself.
     */
    public BasicRelationship setSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    /**
     * Gets the unique Id of the target digital twin. This field is present on every relationship.
     * @return The unique Id of the target digital twin. This field is present on every relationship.
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * Sets the unique Id of the target digital twin. This field is present on every relationship.
     * @param targetId The unique Id of the target digital twin. This field is present on every relationship.
     * @return The BasicRelationship object itself.
     */
    public BasicRelationship setTargetId(String targetId) {
        this.targetId = targetId;
        return this;
    }

    /**
     * Gets the name of the relationship, which defines the type of link (e.g. Contains). This field is present on every relationship.
     * @return The name of the relationship, which defines the type of link (e.g. Contains). This field is present on every relationship.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the relationship, which defines the type of link (e.g. Contains). This field is present on every relationship.
     * @param name The name of the relationship, which defines the type of link (e.g. Contains). This field is present on every relationship.
     * @return The BasicRelationship object itself.
     */
    public BasicRelationship setName(String name) {
        this.name = name;
        return this;
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
     * Sets the additional properties defined in the model. This field will contain any properties of the relationship that are not already defined by the other strong types of this class.
     * @param key The key of the additional property to be added to the relationship.
     * @param value The value of the additional property to be added to the relationship.
     * @return The BasicRelationship object itself.
     */
    @JsonAnySetter
    public BasicRelationship setCustomProperties(String key, Object value) {
        this.customProperties.put(key, value);
        return this;
    }

}
