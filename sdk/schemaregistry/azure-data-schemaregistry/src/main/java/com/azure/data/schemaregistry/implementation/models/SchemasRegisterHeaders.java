// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The SchemasRegisterHeaders model. */
@Fluent
public final class SchemasRegisterHeaders {
    /*
     * The X-Schema-Version property.
     */
    @JsonProperty(value = "schema-version")
    private Integer schemaVersion;

    /*
     * The X-Schema-Type property.
     */
    @JsonProperty(value = "serialization-type")
    private String schemaType;

    /*
     * The X-Schema-Id property.
     */
    @JsonProperty(value = "schema-id")
    private String schemaId;

    /*
     * The X-Schema-Id-Location property.
     */
    @JsonProperty(value = "schema-id-location")
    private String schemaIdLocation;

    /*
     * The Location property.
     */
    @JsonProperty(value = "location")
    private String location;

    /**
     * Get the SchemaVersion property: The Schema-Version property.
     *
     * @return the SchemaVersion value.
     */
    public Integer getSchemaVersion() {
        return this.schemaVersion;
    }

    /**
     * Set the SchemaVersion property: The Schema-Version property.
     *
     * @param SchemaVersion the SchemaVersion value to set.
     * @return the SchemasRegisterHeaders object itself.
     */
    public SchemasRegisterHeaders setSchemaVersion(Integer SchemaVersion) {
        this.schemaVersion = SchemaVersion;
        return this;
    }

    /**
     * Get the SchemaType property: The Schema-Type property.
     *
     * @return the SchemaType value.
     */
    public String getSchemaType() {
        return this.schemaType;
    }

    /**
     * Set the SchemaType property: The Schema-Type property.
     *
     * @param SchemaType the SchemaType value to set.
     * @return the SchemasRegisterHeaders object itself.
     */
    public SchemasRegisterHeaders setSchemaType(String SchemaType) {
        this.schemaType = SchemaType;
        return this;
    }

    /**
     * Get the SchemaId property: The Schema-Id property.
     *
     * @return the SchemaId value.
     */
    public String getSchemaId() {
        return this.schemaId;
    }

    /**
     * Set the SchemaId property: The Schema-Id property.
     *
     * @param SchemaId the SchemaId value to set.
     * @return the SchemasRegisterHeaders object itself.
     */
    public SchemasRegisterHeaders setSchemaId(String SchemaId) {
        this.schemaId = SchemaId;
        return this;
    }

    /**
     * Get the SchemaIdLocation property: The Schema-Id-Location property.
     *
     * @return the SchemaIdLocation value.
     */
    public String getSchemaIdLocation() {
        return this.schemaIdLocation;
    }

    /**
     * Set the SchemaIdLocation property: The Schema-Id-Location property.
     *
     * @param SchemaIdLocation the SchemaIdLocation value to set.
     * @return the SchemasRegisterHeaders object itself.
     */
    public SchemasRegisterHeaders setSchemaIdLocation(String SchemaIdLocation) {
        this.schemaIdLocation = SchemaIdLocation;
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
     * @return the SchemasRegisterHeaders object itself.
     */
    public SchemasRegisterHeaders setLocation(String location) {
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
