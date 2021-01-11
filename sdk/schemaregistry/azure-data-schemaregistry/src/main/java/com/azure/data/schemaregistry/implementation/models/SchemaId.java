// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The SchemaId model. */
@Fluent
public final class SchemaId {
    /*
     * Schema ID that uniquely identifies a schema in the registry namespace.
     */
    @JsonProperty(value = "id")
    private String id;

    /**
     * Get the id property: Schema ID that uniquely identifies a schema in the registry namespace.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: Schema ID that uniquely identifies a schema in the registry namespace.
     *
     * @param id the id value to set.
     * @return the SchemaId object itself.
     */
    public SchemaId setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {}
}
