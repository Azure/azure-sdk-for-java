// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.containers.containerregistry.implementation.ArtifactTagPropertiesHelper;
import com.azure.containers.containerregistry.implementation.models.ArtifactTagPropertiesInternal;
import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;

/** Tag attributes. */
@Fluent
public final class ArtifactTagProperties implements JsonSerializable<ArtifactTagProperties> {
    private final ArtifactTagPropertiesInternal internal;

    static {
        ArtifactTagPropertiesHelper.setAccessor(ArtifactTagProperties::new);
    }

    /** Creates an instance of ArtifactTagProperties class. */
    public ArtifactTagProperties() {
        this(new ArtifactTagPropertiesInternal());
    }

    private ArtifactTagProperties(ArtifactTagPropertiesInternal internal) {
        this.internal = internal;
    }

    /**
     * Get the registryLoginServer property: Registry login server name. This is likely to be similar to
     * {registry-name}.azurecr.io.
     *
     * @return the registryLoginServer value.
     */
    public String getRegistryLoginServer() {
        return internal.getRegistryLoginServer();
    }

    /**
     * Get the repositoryName property: Image name.
     *
     * @return the repositoryName value.
     */
    public String getRepositoryName() {
        return internal.getRepositoryName();
    }

    /**
     * Get the name property: Tag name.
     *
     * @return the name value.
     */
    public String getName() {
        return internal.getName();
    }

    /**
     * Get the digest property: Tag digest.
     *
     * @return the digest value.
     */
    public String getDigest() {
        return internal.getDigest();
    }

    /**
     * Get the createdOn property: Tag created time.
     *
     * @return the createdOn value.
     */
    public OffsetDateTime getCreatedOn() {
        return internal.getCreatedOn();
    }

    /**
     * Get the lastUpdatedOn property: Tag last update time.
     *
     * @return the lastUpdatedOn value.
     */
    public OffsetDateTime getLastUpdatedOn() {
        return internal.getLastUpdatedOn();
    }

    /**
     * Get the deleteEnabled property: Delete enabled.
     *
     * @return the deleteEnabled value.
     */
    public Boolean isDeleteEnabled() {
        return internal.isDeleteEnabled();
    }

    /**
     * Set the deleteEnabled property: Delete enabled.
     *
     * @param deleteEnabled the deleteEnabled value to set.
     * @return the ArtifactTagProperties object itself.
     */
    public ArtifactTagProperties setDeleteEnabled(Boolean deleteEnabled) {
        internal.setDeleteEnabled(deleteEnabled);
        return this;
    }

    /**
     * Get the writeEnabled property: Write enabled.
     *
     * @return the writeEnabled value.
     */
    public Boolean isWriteEnabled() {
        return internal.isWriteEnabled();
    }

    /**
     * Set the writeEnabled property: Write enabled.
     *
     * @param writeEnabled the writeEnabled value to set.
     * @return the ArtifactTagProperties object itself.
     */
    public ArtifactTagProperties setWriteEnabled(Boolean writeEnabled) {
        internal.setWriteEnabled(writeEnabled);
        return this;
    }

    /**
     * Get the listEnabled property: List enabled.
     *
     * @return the listEnabled value.
     */
    public Boolean isListEnabled() {
        return internal.isListEnabled();
    }

    /**
     * Set the listEnabled property: List enabled.
     *
     * @param listEnabled the listEnabled value to set.
     * @return the ArtifactTagProperties object itself.
     */
    public ArtifactTagProperties setListEnabled(Boolean listEnabled) {
        internal.setListEnabled(listEnabled);
        return this;
    }

    /**
     * Get the readEnabled property: Read enabled.
     *
     * @return the readEnabled value.
     */
    public Boolean isReadEnabled() {
        return internal.isReadEnabled();
    }

    /**
     * Set the readEnabled property: Read enabled.
     *
     * @param readEnabled the readEnabled value to set.
     * @return the ArtifactTagProperties object itself.
     */
    public ArtifactTagProperties setReadEnabled(Boolean readEnabled) {
        internal.setReadEnabled(readEnabled);
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return internal.toJson(jsonWriter);
    }

    /**
     * Reads an instance of ArtifactTagProperties from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ArtifactTagProperties if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ArtifactTagProperties.
     */
    public static ArtifactTagProperties fromJson(JsonReader jsonReader) throws IOException {
        ArtifactTagPropertiesInternal internal = ArtifactTagPropertiesInternal.fromJson(jsonReader);
        return internal == null ? null : new ArtifactTagProperties(internal);
    }
}
