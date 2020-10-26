// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Interface that defines the Json properties that are present in every digital twin relationship. Implementations of
 * this class must add Json properties for each of the getters defined in this interface. See each getter for more
 * details on each Json property.
 */
public interface IDigitalTwinRelationship {
    public static final String DIGITAL_TWIN_RELATIONSHIP_ID_JSON_PROPERTY_NAME = "$relationshipId";
    public static final String DIGITAL_TWIN_RELATIONSHIP_SOURCE_ID_JSON_PROPERTY_NAME = "$sourceId";
    public static final String DIGITAL_TWIN_RELATIONSHIP_TARGET_ID_JSON_PROPERTY_NAME = "$targetId";
    public static final String DIGITAL_TWIN_RELATIONSHIP_NAME_JSON_PROPERTY_NAME = "$relationshipName";

    /**
     * Gets the unique Id of the relationship. This field is present on every relationship.
     * <p>
     * The Json property associated with this Id is {@link IDigitalTwinRelationship#DIGITAL_TWIN_RELATIONSHIP_ID_JSON_PROPERTY_NAME},
     * so every implementation of this class must have a Json property with that value. As an example, see
     * {@link com.azure.digitaltwins.core.models.BasicRelationship} which demonstrates how to create this
     * property using {@link JsonProperty}.
     *
     * @return The unique Id of the relationship. This field is present on every relationship.
     */
    public String getRelationshipId();

    /**
     * Gets the unique Id of the source digital twin. This field is present on every relationship.
     * <p>
     * The Json property associated with this source digital twin Id is
     * {@link IDigitalTwinRelationship#DIGITAL_TWIN_RELATIONSHIP_SOURCE_ID_JSON_PROPERTY_NAME},
     * so every implementation of this class must have a Json property with that value. As an example, see
     * {@link com.azure.digitaltwins.core.models.BasicRelationship} which demonstrates how to create this
     * property using {@link JsonProperty}.
     *
     * @return The unique Id of the source digital twin. This field is present on every relationship.
     */
    public String getSourceDigitalTwinId();

    /**
     * Gets the unique Id of the target digital twin. This field is present on every relationship.
     * <p>
     * The Json property associated with this target digital twin Id is
     * {@link IDigitalTwinRelationship#DIGITAL_TWIN_RELATIONSHIP_TARGET_ID_JSON_PROPERTY_NAME},
     * so every implementation of this class must have a Json property with that value. As an example, see
     * {@link com.azure.digitaltwins.core.models.BasicRelationship} which demonstrates how to create this
     * property using {@link JsonProperty}.
     *
     * @return The unique Id of the target digital twin. This field is present on every relationship.
     */
    public String getTargetDigitalTwinId();

    /**
     * Gets the name of the relationship, which defines the type of link (e.g. Contains). This field is present on every relationship.
     * <p>
     * The Json property associated with this relationship name is
     * {@link IDigitalTwinRelationship#DIGITAL_TWIN_RELATIONSHIP_NAME_JSON_PROPERTY_NAME},
     * so every implementation of this class must have a Json property with that value. As an example, see
     * {@link com.azure.digitaltwins.core.models.BasicRelationship} which demonstrates how to create this
     * property using {@link JsonProperty}.
     *
     * @return The name of the relationship, which defines the type of link (e.g. Contains). This field is present on every relationship.
     */
    public String getRelationshipName();
}
