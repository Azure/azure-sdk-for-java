// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.azure.digitaltwins.core.IDigitalTwinRelationship;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Although relationships have a user-defined schema, these properties should exist on every instance.
 * This is useful to use as a base class to ensure your custom relationships have the necessary properties.
 *
 * <p>
 * Note that this class extends the {@link IDigitalTwinRelationship} interface so it can be used as the input type for calls such
 * as {@link com.azure.digitaltwins.core.DigitalTwinsClient#createRelationship(String, String, IDigitalTwinRelationship, Class)}.
 */
@Fluent
@JsonInclude(Include.NON_NULL)
public final class BasicRelationship implements IDigitalTwinRelationship {

    @JsonProperty(value = IDigitalTwinRelationship.DIGITAL_TWIN_RELATIONSHIP_ID_JSON_PROPERTY_NAME, required = true)
    private String id;

    @JsonProperty(value = IDigitalTwinRelationship.DIGITAL_TWIN_RELATIONSHIP_SOURCE_ID_JSON_PROPERTY_NAME, required = true)
    private String sourceId;

    @JsonProperty(value = IDigitalTwinRelationship.DIGITAL_TWIN_RELATIONSHIP_TARGET_ID_JSON_PROPERTY_NAME, required = true)
    private String targetId;

    @JsonProperty(value = IDigitalTwinRelationship.DIGITAL_TWIN_RELATIONSHIP_NAME_JSON_PROPERTY_NAME, required = true)
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
