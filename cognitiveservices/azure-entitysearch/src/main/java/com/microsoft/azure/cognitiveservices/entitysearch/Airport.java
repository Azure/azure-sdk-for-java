/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The Airport model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("Airport")
public class Airport extends CivicStructure {
    /**
     * The iataCode property.
     */
    @JsonProperty(value = "iataCode", access = JsonProperty.Access.WRITE_ONLY)
    private String iataCode;

    /**
     * The icaoCode property.
     */
    @JsonProperty(value = "icaoCode", access = JsonProperty.Access.WRITE_ONLY)
    private String icaoCode;

    /**
     * Get the iataCode value.
     *
     * @return the iataCode value
     */
    public String iataCode() {
        return this.iataCode;
    }

    /**
     * Get the icaoCode value.
     *
     * @return the icaoCode value
     */
    public String icaoCode() {
        return this.icaoCode;
    }

}
