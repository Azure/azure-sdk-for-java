// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.core.annotation.Immutable;
import java.time.OffsetDateTime;
import java.util.List;

/** Manifest attributes details. */
@Immutable
public final class RegistryArtifactProperties {
    /*
     * Manifest
     */
    private final String digest;

    /*
     * Image size
     */
    private final Long size;

    /*
     * Created time
     */
    private final OffsetDateTime createdOn;

    /*
     * Last update time
     */
    private final OffsetDateTime lastUpdatedOn;

    /*
     * CPU architecture
     */
    private final String cpuArchitecture;

    /*
     * Operating system
     */
    private final String operatingSystem;

    /*
     * List of manifest attributes details
     */
    private final List<RegistryArtifactProperties> references;

    /*
     * List of tags
     */
    private final List<String> tags;

    /*
     * Writeable properties of the resource
     */
    private final ContentProperties writeableProperties;

    /**
     * Create an instance of RegistryArtifactProperties class.
     * @param digest digest associated with the registry artifact properties.
     * @param writeableProperties writeable properties.
     * @param registryArtifacts the artifacts associated with the registry.
     * @param cpuArchitecture cpu architecture of the image.
     * @param operatingSystem operation system associated with the image.
     * @param createdOn the date on which the artifact was created.
     * @param lastUpdatedOn the date on which the artifact was last updated.
     * @param tags tags associated with the artifacts.
     * @param size size associated with the artifacts.
     */
    public RegistryArtifactProperties(
        String digest,
        ContentProperties writeableProperties,
        List<RegistryArtifactProperties> registryArtifacts,
        String cpuArchitecture,
        String operatingSystem,
        OffsetDateTime createdOn,
        OffsetDateTime lastUpdatedOn,
        List<String> tags,
        Long size) {
        this.digest = digest;
        this.writeableProperties = writeableProperties;
        this.references = registryArtifacts;
        this.cpuArchitecture = cpuArchitecture;
        this.operatingSystem = operatingSystem;
        this.createdOn = createdOn;
        this.lastUpdatedOn = lastUpdatedOn;
        this.size = size;
        this.tags = tags;
    }

    /**
     * Get the digest property: Manifest.
     *
     * @return the digest value.
     */
    public String getDigest() {
        return this.digest;
    }

    /**
     * Get the size property: Image size.
     *
     * @return the size value.
     */
    public Long getSize() {
        return this.size;
    }

    /**
     * Get the createdOn property: Created time.
     *
     * @return the createdOn value.
     */
    public OffsetDateTime getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Get the lastUpdatedOn property: Last update time.
     *
     * @return the lastUpdatedOn value.
     */
    public OffsetDateTime getLastUpdatedOn() {
        return this.lastUpdatedOn;
    }

    /**
     * Get the cpuArchitecture property: CPU architecture.
     *
     * @return the cpuArchitecture value.
     */
    public String getCpuArchitecture() {
        return this.cpuArchitecture;
    }


    /**
     * Get the operatingSystem property: Operating system.
     *
     * @return the operatingSystem value.
     */
    public String getOperatingSystem() {
        return this.operatingSystem;
    }

    /**
     * Get the references property: List of manifest attributes details.
     *
     * @return the references value.
     */
    public List<RegistryArtifactProperties> getRegistryArtifacts() {
        return this.references;
    }

    /**
     * Get the tags property: List of tags.
     *
     * @return the tags value.
     */
    public List<String> getTags() {
        return this.tags;
    }

    /**
     * Get the writeableProperties property: Writeable properties of the resource.
     *
     * @return the writeableProperties value.
     */
    public ContentProperties getWriteableProperties() {
        return this.writeableProperties;
    }
}
