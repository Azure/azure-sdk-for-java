// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.Utils;

/**
 * Encapsulates the container link associated with the container.
 * */
public final class CosmosContainerIdentity {

    private final String containerLink;


    /**
     * Instantiates a {@link CosmosContainerIdentity} class
     *
     * @param databaseName the name of the database
     * @param containerName the name of the container
     * */
    public CosmosContainerIdentity(String databaseName, String containerName) {

        if (databaseName == null || databaseName.isEmpty()) {
            throw new IllegalArgumentException("databaseName is null or empty");
        }

        if (containerName == null || containerName.isEmpty()) {
            throw new IllegalArgumentException("containerName is null or empty");
        }

        String databaseLink = Utils.joinPath(Paths.DATABASES_ROOT, databaseName);
        this.containerLink = Utils.joinPath(databaseLink, Paths.COLLECTIONS_PATH_SEGMENT) + containerName;
    }

    /**
     * Instantiates a {@link CosmosContainerIdentity} class
     *
     * @param containerLink the fully qualified name of the container
     * */
    public CosmosContainerIdentity(String containerLink) {

        if (containerLink == null || containerLink.isEmpty()) {
            throw new IllegalArgumentException("containerLink is null or empty");
        }

        this.containerLink = containerLink;
    }

    /**
     * Gets the fully qualified name of the container
     * */
    public String getContainerLink() {
        return containerLink;
    }
}
