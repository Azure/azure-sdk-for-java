// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.containers.containerregistry.implementation.ArtifactManifestPropertiesHelper;
import com.azure.containers.containerregistry.implementation.models.ArtifactManifestPropertiesInternal;
import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

/** Manifest attributes details. */
@Fluent
public final class ArtifactManifestProperties implements JsonSerializable<ArtifactManifestProperties> {
    private final ArtifactManifestPropertiesInternal internal;

    static {
        ArtifactManifestPropertiesHelper.setAccessor(ArtifactManifestProperties::new);
    }

    /** Creates an instance of ArtifactManifestProperties class. */
    public ArtifactManifestProperties() {
        this(new ArtifactManifestPropertiesInternal());
    }

    private ArtifactManifestProperties(ArtifactManifestPropertiesInternal internal) {
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
     * Get the repositoryName property: Repository name.
     *
     * @return the repositoryName value.
     */
    public String getRepositoryName() {
        return internal.getRepositoryName();
    }

    /**
     * Get the digest property: Manifest.
     *
     * @return the digest value.
     */
    public String getDigest() {
        return internal.getDigest();
    }

    /**
     * Get the sizeInBytes property: Image size in bytes.
     *
     * @return the size value.
     */
    public Long getSizeInBytes() {
        return internal.getSizeInBytes();
    }

    /**
     * Get the createdOn property: Created time.
     *
     * @return the createdOn value.
     */
    public OffsetDateTime getCreatedOn() {
        return internal.getCreatedOn();
    }

    /**
     * Get the lastUpdatedOn property: Last update time.
     *
     * @return the lastUpdatedOn value.
     */
    public OffsetDateTime getLastUpdatedOn() {
        return internal.getLastUpdatedOn();
    }

    /**
     * Get the architecture property: CPU architecture.
     *
     * @return the architecture value.
     */
    public ArtifactArchitecture getArchitecture() {
        return internal.getArchitecture();
    }

    /**
     * Get the operatingSystem property: Operating system.
     *
     * @return the operatingSystem value.
     */
    public ArtifactOperatingSystem getOperatingSystem() {
        return internal.getOperatingSystem();
    }

    /**
     * Get the relatedArtifacts property: List of artifacts that are referenced by this manifest list, with information
     * about the platform each supports. This list will be empty if this is a leaf manifest and not a manifest list.
     *
     * @return the relatedArtifacts value.
     */
    public List<ArtifactManifestPlatform> getRelatedArtifacts() {
        return internal.getRelatedArtifacts();
    }

    /**
     * Get the tags property: List of tags.
     *
     * @return the tags value.
     */
    public List<String> getTags() {
        return internal.getTags();
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
     * @return the ArtifactManifestProperties object itself.
     */
    public ArtifactManifestProperties setDeleteEnabled(Boolean deleteEnabled) {
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
     * @return the ArtifactManifestProperties object itself.
     */
    public ArtifactManifestProperties setWriteEnabled(Boolean writeEnabled) {
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
     * @return the ArtifactManifestProperties object itself.
     */
    public ArtifactManifestProperties setListEnabled(Boolean listEnabled) {
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
     * @return the ArtifactManifestProperties object itself.
     */
    public ArtifactManifestProperties setReadEnabled(Boolean readEnabled) {
        internal.setReadEnabled(readEnabled);
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return internal.toJson(jsonWriter);
    }

    /**
     * Reads an instance of ArtifactManifestProperties from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ArtifactManifestProperties if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ArtifactManifestProperties.
     */
    public static ArtifactManifestProperties fromJson(JsonReader jsonReader) throws IOException {
        ArtifactManifestPropertiesInternal internal = ArtifactManifestPropertiesInternal.fromJson(jsonReader);
        return internal == null ? null : new ArtifactManifestProperties(internal);
    }
}
