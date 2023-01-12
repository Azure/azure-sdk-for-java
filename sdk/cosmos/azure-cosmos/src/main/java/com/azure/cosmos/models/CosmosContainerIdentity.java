// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.Strings;
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

        if (Strings.isNullOrWhiteSpace(databaseName)) {
            throw new IllegalArgumentException("databaseName is either null or empty");
        }

        if (Strings.isNullOrWhiteSpace(containerName)) {
            throw new IllegalArgumentException("containerName is either null or empty");
        }

        String databaseLink = Utils.joinPath(Paths.DATABASES_ROOT, databaseName);
        this.containerLink = Utils.joinPath(databaseLink, Paths.COLLECTIONS_PATH_SEGMENT) + containerName;
    }

    /**
     * Gets the fully qualified name of the container
     * */
    public String getContainerLink() {
        return containerLink;
    }
}
