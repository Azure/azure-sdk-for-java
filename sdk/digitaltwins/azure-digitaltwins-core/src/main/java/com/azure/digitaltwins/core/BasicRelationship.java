// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.Fluent;
import com.azure.digitaltwins.core.models.DigitalTwinsJsonPropertyNames;
import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

/**
 * Although relationships have a user-defined schema, these properties should exist on every instance.
 * This is useful to use as a base class to ensure your custom relationships have the necessary properties.
 * <p>
 * Note that this class uses {@link JsonProperty} from the Jackson serialization library. Because of this, this type
 * will only work if the default json serializer is used by the digital twins client or if the custom json
 * serializer uses Jackson as well. In order to use a different json library, a new BasicRelationship class must
 * be constructed and have its json properties tagged by the annotation used by that json library.
 */
@Fluent
@JsonInclude(Include.NON_NULL)
public final class BasicRelationship {

    @JsonProperty(value = DigitalTwinsJsonPropertyNames.RELATIONSHIP_ID, required = true)
    private String id;

    @JsonProperty(value = DigitalTwinsJsonPropertyNames.RELATIONSHIP_SOURCE_ID, required = true)
    private String sourceId;

    @JsonProperty(value = DigitalTwinsJsonPropertyNames.RELATIONSHIP_TARGET_ID, required = true)
    private String targetId;

    @JsonProperty(value = DigitalTwinsJsonPropertyNames.RELATIONSHIP_NAME, required = true)
    private String name;

    @JsonProperty(value = DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ETAG)
    private String etag;

    @JsonIgnore
    private final Map<String, Object> properties = new HashMap<>();

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
    public String getId() {
        return id;
    }

    /**
     * Gets the unique Id of the source digital twin. This field is present on every relationship.
     * @return The unique Id of the source digital twin. This field is present on every relationship.
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * Gets the unique Id of the target digital twin. This field is present on every relationship.
     * @return The unique Id of the target digital twin. This field is present on every relationship.
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * Gets the name of the relationship, which defines the type of link (e.g. Contains). This field is present on every relationship.
     * @return The name of the relationship, which defines the type of link (e.g. Contains). This field is present on every relationship.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     * @return A string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     */
    public String getETag() {
        return etag;
    }

    /**
     * Sets a string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     * @param etag A string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     * @return The BasicRelationship object itself.
     */
    public BasicRelationship setETag(String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * Gets the additional custom properties defined in the model. This field will contain any properties of the relationship that are not already defined by the other strong types of this class.
     * @return The additional custom properties defined in the model. This field will contain any properties of the relationship that are not already defined by the other strong types of this class.
     */
    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Adds an additional custom property to this model. This field will contain any properties of the relationship that are not already defined by the other strong types of this class.
     * @param key The key of the additional property to be added to the relationship.
     * @param value The value of the additional property to be added to the relationship.
     * @return The BasicRelationship object itself.
     */
    @JsonAnySetter
    public BasicRelationship addProperty(String key, Object value) {
        this.properties.put(key, value);
        return this;
    }

}
