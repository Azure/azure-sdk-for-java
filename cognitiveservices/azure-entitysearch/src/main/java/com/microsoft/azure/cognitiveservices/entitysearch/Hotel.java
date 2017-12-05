/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The Hotel model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("Hotel")
public class Hotel extends LodgingBusiness {
    /**
     * The hotelClass property.
     */
    @JsonProperty(value = "hotelClass", access = JsonProperty.Access.WRITE_ONLY)
    private String hotelClass;

    /**
     * The amenities property.
     */
    @JsonProperty(value = "amenities", access = JsonProperty.Access.WRITE_ONLY)
    private List<String> amenities;

    /**
     * Get the hotelClass value.
     *
     * @return the hotelClass value
     */
    public String hotelClass() {
        return this.hotelClass;
    }

    /**
     * Get the amenities value.
     *
     * @return the amenities value
     */
    public List<String> amenities() {
        return this.amenities;
    }

}
