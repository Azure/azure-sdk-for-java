// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.digitaltwins.core.models.DigitalTwinMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Interface that defines the Json properties that are present in every digital twin. Implementations of this class
 * must add Json properties for each of the getters defined in this interface. See each getter for more
 * details on each Json property.
 */
public interface IDigitalTwin {
    public static String DIGITAL_TWIN_ID_JSON_PROPERTY_NAME = "$dtId";
    public static String DIGITAL_TWIN_ETAG_JSON_PROPERTY_NAME = "$etag";
    public static String DIGITAL_TWIN__METADATA_JSON_PROPERTY_NAME = "$metadata";

    /**
     * Gets the unique Id of the digital twin in a digital twins instance. This field is present on every digital twin.
     * <p>
     * The Json property associated with this Id is {@link IDigitalTwin#DIGITAL_TWIN_ID_JSON_PROPERTY_NAME},
     * so every implementation of this class must have a Json property with that value. As an example, see
     * {@link com.azure.digitaltwins.core.models.BasicDigitalTwin} which demonstrates how to create this
     * property using {@link JsonProperty}.
     *
     * @return The unique Id of the digital twin in a digital twins instance. This field is present on every digital twin.
     */
    public String getId();

    /**
     * Gets a string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     * <p>
     * The Json property associated with this ETag is {@link IDigitalTwin#DIGITAL_TWIN_ETAG_JSON_PROPERTY_NAME},
     * so every implementation of this class must have a Json property with that value. As an example, see
     * {@link com.azure.digitaltwins.core.models.BasicDigitalTwin} which demonstrates how to create this
     * property using {@link JsonProperty}.
     *
     * @return A string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     */
    public String getETag();

    /**
     * Gets the information about the model a digital twin conforms to. This field is present on every digital twin.
     * <p>
     * The Json property associated with this Metadata is {@link IDigitalTwin#DIGITAL_TWIN__METADATA_JSON_PROPERTY_NAME},
     * so every implementation of this class must have a Json property with that value. As an example, see
     * {@link com.azure.digitaltwins.core.models.BasicDigitalTwin} which demonstrates how to create this
     * property using {@link JsonProperty}.
     *
     * @return The information about the model a digital twin conforms to. This field is present on every digital twin.
     */
    public IDigitalTwinMetadata getMetadata();
}
