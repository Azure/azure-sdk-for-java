// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Interface that defines the Json properties that are present in every digital twin metadata object. Implementations of
 * this class must add Json properties for each of the getters defined in this interface. See each getter for more
 * details on each Json property.
 */
public interface IDigitalTwinMetadata {

    public static String DIGITAL_TWIN_METADATA_MODEL_ID_JSON_PROPERTY_NAME = "$model";

    /**
     * Gets the Id of the model that the digital twin or component is modeled by.
     * <p>
     * The Json property associated with this model Id is {@link IDigitalTwinMetadata#DIGITAL_TWIN_METADATA_MODEL_ID_JSON_PROPERTY_NAME},
     * so every implementation of this class must have a Json property with that value. As an example, see
     * {@link com.azure.digitaltwins.core.models.DigitalTwinMetadata} which demonstrates how to create this
     * property using {@link JsonProperty}.
     *
     * @return The Id of the model that the digital twin or component is modeled by.
     */
    public String getModelId();
}
