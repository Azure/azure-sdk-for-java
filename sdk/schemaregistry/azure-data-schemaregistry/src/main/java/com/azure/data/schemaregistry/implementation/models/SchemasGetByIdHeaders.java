// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The SchemasGetByIdHeaders model. */
@Fluent
public final class SchemasGetByIdHeaders {
    /*
     * The schema-Version property.
     */
    @JsonProperty(value = "schema-version")
    private Integer schemaVersion;

    /*
     * The schema-Type property.
     */
    @JsonProperty(value = "serialization-type")
    private String schemaType;

    /*
     * The schema-Id property.
     */
    @JsonProperty(value = "schema-id")
    private String schemaId;

    /*
     * The schema-Id-Location property.
     */
    @JsonProperty(value = "schema-id-location")
    private String schemaIdLocation;

    /*
     * The Location property.
     */
    @JsonProperty(value = "location")
    private String location;

    /**
     * Get the schemaVersion property: The Schema-Version property.
     *
     * @return the schemaVersion value.
     */
    public Integer getSchemaVersion() {
        return this.schemaVersion;
    }

    /**
     * Set the schemaVersion property: The schema-Version property.
     *
     * @param schemaVersion the schemaVersion value to set.
     * @return the SchemasGetByIdHeaders object itself.
     */
    public SchemasGetByIdHeaders setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
        return this;
    }

    /**
     * Get the schemaType property: The schema-Type property.
     *
     * @return the schemaType value.
     */
    public String getSchemaType() {
        return this.schemaType;
    }

    /**
     * Set the schemaType property: The schema-Type property.
     *
     * @param schemaType the schemaType value to set.
     * @return the SchemasGetByIdHeaders object itself.
     */
    public SchemasGetByIdHeaders setSchemaType(String schemaType) {
        this.schemaType = schemaType;
        return this;
    }

    /**
     * Get the schemaId property: The schema-Id property.
     *
     * @return the schemaId value.
     */
    public String getSchemaId() {
        return this.schemaId;
    }

    /**
     * Set the schemaId property: The schema-Id property.
     *
     * @param schemaId the schemaId value to set.
     * @return the SchemasGetByIdHeaders object itself.
     */
    public SchemasGetByIdHeaders setSchemaId(String schemaId) {
        this.schemaId = schemaId;
        return this;
    }

    /**
     * Get the schemaIdLocation property: The schema-Id-Location property.
     *
     * @return the schemaIdLocation value.
     */
    public String getSchemaIdLocation() {
        return this.schemaIdLocation;
    }

    /**
     * Set the schemaIdLocation property: The schema-Id-Location property.
     *
     * @param schemaIdLocation the schemaIdLocation value to set.
     * @return the SchemasGetByIdHeaders object itself.
     */
    public SchemasGetByIdHeaders setSchemaIdLocation(String schemaIdLocation) {
        this.schemaIdLocation = schemaIdLocation;
        return this;
    }

    /**
     * Get the location property: The Location property.
     *
     * @return the location value.
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Set the location property: The Location property.
     *
     * @param location the location value to set.
     * @return the SchemasGetByIdHeaders object itself.
     */
    public SchemasGetByIdHeaders setLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {}
}
