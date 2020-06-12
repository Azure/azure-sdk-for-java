// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.client.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.Map;

/** The SchemaGroup model. */
@Fluent
public final class SchemaGroup {
    /*
     * The name property.
     */
    @JsonProperty(value = "name")
    private String name;

    /*
     * The createdTimeUtc property.
     */
    @JsonProperty(value = "createdTimeUtc")
    private OffsetDateTime createdTimeUtc;

    /*
     * The updatedTimeUtc property.
     */
    @JsonProperty(value = "updatedTimeUtc")
    private OffsetDateTime updatedTimeUtc;

    /*
     * The schemaType property.
     */
    @JsonProperty(value = "schemaType")
    private String schemaType;

    /*
     * schema compatibility mode enum, defined by supported schema type
     */
    @JsonProperty(value = "schemaCompatibility")
    private Integer schemaCompatibility;

    /*
     * Dictionary of <string>
     */
    @JsonProperty(value = "groupProperties")
    private Map<String, String> groupProperties;

    /**
     * Get the name property: The name property.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: The name property.
     *
     * @param name the name value to set.
     * @return the SchemaGroup object itself.
     */
    public SchemaGroup setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the createdTimeUtc property: The createdTimeUtc property.
     *
     * @return the createdTimeUtc value.
     */
    public OffsetDateTime getCreatedTimeUtc() {
        return this.createdTimeUtc;
    }

    /**
     * Set the createdTimeUtc property: The createdTimeUtc property.
     *
     * @param createdTimeUtc the createdTimeUtc value to set.
     * @return the SchemaGroup object itself.
     */
    public SchemaGroup setCreatedTimeUtc(OffsetDateTime createdTimeUtc) {
        this.createdTimeUtc = createdTimeUtc;
        return this;
    }

    /**
     * Get the updatedTimeUtc property: The updatedTimeUtc property.
     *
     * @return the updatedTimeUtc value.
     */
    public OffsetDateTime getUpdatedTimeUtc() {
        return this.updatedTimeUtc;
    }

    /**
     * Set the updatedTimeUtc property: The updatedTimeUtc property.
     *
     * @param updatedTimeUtc the updatedTimeUtc value to set.
     * @return the SchemaGroup object itself.
     */
    public SchemaGroup setUpdatedTimeUtc(OffsetDateTime updatedTimeUtc) {
        this.updatedTimeUtc = updatedTimeUtc;
        return this;
    }

    /**
     * Get the schemaType property: The schemaType property.
     *
     * @return the schemaType value.
     */
    public String getSchemaType() {
        return this.schemaType;
    }

    /**
     * Set the schemaType property: The schemaType property.
     *
     * @param schemaType the schemaType value to set.
     * @return the SchemaGroup object itself.
     */
    public SchemaGroup setSchemaType(String schemaType) {
        this.schemaType = schemaType;
        return this;
    }

    /**
     * Get the schemaCompatibility property: schema compatibility mode enum, defined by supported schema type.
     *
     * @return the schemaCompatibility value.
     */
    public Integer getSchemaCompatibility() {
        return this.schemaCompatibility;
    }

    /**
     * Set the schemaCompatibility property: schema compatibility mode enum, defined by supported schema type.
     *
     * @param schemaCompatibility the schemaCompatibility value to set.
     * @return the SchemaGroup object itself.
     */
    public SchemaGroup setSchemaCompatibility(Integer schemaCompatibility) {
        this.schemaCompatibility = schemaCompatibility;
        return this;
    }

    /**
     * Get the groupProperties property: Dictionary of &lt;string&gt;.
     *
     * @return the groupProperties value.
     */
    public Map<String, String> getGroupProperties() {
        return this.groupProperties;
    }

    /**
     * Set the groupProperties property: Dictionary of &lt;string&gt;.
     *
     * @param groupProperties the groupProperties value to set.
     * @return the SchemaGroup object itself.
     */
    public SchemaGroup setGroupProperties(Map<String, String> groupProperties) {
        this.groupProperties = groupProperties;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() { }
}
