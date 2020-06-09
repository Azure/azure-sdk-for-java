// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.client.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The GetSchemaVersionsHeaders model. */
@Fluent
public final class GetSchemaVersionsHeaders {
    /*
     * The X-Schema-Type property.
     */
    @JsonProperty(value = "X-Schema-Type")
    private String xSchemaType;

    /**
     * Get the xSchemaType property: The X-Schema-Type property.
     *
     * @return the xSchemaType value.
     */
    public String getXSchemaType() {
        return this.xSchemaType;
    }

    /**
     * Set the xSchemaType property: The X-Schema-Type property.
     *
     * @param xSchemaType the xSchemaType value to set.
     * @return the GetSchemaVersionsHeaders object itself.
     */
    public GetSchemaVersionsHeaders setXSchemaType(String xSchemaType) {
        this.xSchemaType = xSchemaType;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() { }
}
