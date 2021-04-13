// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.core.annotation.Fluent;

/**
 * List the registry artifact options.
 */
@Fluent
public final class ListRegistryArtifactOptions {
    private RegistryArtifactOrderBy registryArtifactOrderBy;

    /**
     * Initializes an instance of the ListRegistryArtifactOptions
     */
    public ListRegistryArtifactOptions() { }

    /**
     * Gets the order by tag. It represents the order in which the tag results should be ordered.
     *
     * @return The order by tag value.
     */
    public RegistryArtifactOrderBy getRegistryArtifactOrderBy() {
        return this.registryArtifactOrderBy;
    }

    /**
     * Sets the order by tag. It represents the order in which the tag results should be ordered.
     *
     * @param orderBy the order in which the registry artifact operation should be returned.
     * @return The order by tag value.
     */
    public ListRegistryArtifactOptions setRegistryArtifactOrderBy(RegistryArtifactOrderBy orderBy) {
        this.registryArtifactOrderBy = orderBy;
        return this;
    }
}
