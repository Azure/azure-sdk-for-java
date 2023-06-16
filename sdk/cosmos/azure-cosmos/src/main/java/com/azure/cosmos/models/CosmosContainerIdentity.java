// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.Objects;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

/**
 * Encapsulates the container link associated with the container.
 * */
public final class CosmosContainerIdentity {

    private final String databaseName;
    private final String containerName;
    private final String containerLink;

    /**
     * Instantiates a {@link CosmosContainerIdentity} class
     *
     * @param databaseName the name of the database
     * @param containerName the name of the container
     * */
    public CosmosContainerIdentity(String databaseName, String containerName) {

        checkArgument(!Strings.isNullOrWhiteSpace(databaseName), "databaseName should not be null nor empty");
        checkArgument(!Strings.isNullOrWhiteSpace(containerName), "containerName should not be null nor empty");

        this.databaseName = databaseName;
        this.containerName = containerName;
        String databaseLink = Utils.joinPath(Paths.DATABASES_ROOT, databaseName);
        this.containerLink = StringUtils.strip(
                Utils.joinPath(databaseLink, Paths.COLLECTIONS_PATH_SEGMENT) + containerName,
                Constants.Properties.PATH_SEPARATOR);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CosmosContainerIdentity that = (CosmosContainerIdentity) o;
        return Objects.equals(containerLink, that.containerLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containerLink);
    }

    static void initialize() {
        ImplementationBridgeHelpers.CosmosContainerIdentityHelper.setCosmosContainerIdentityAccessor(new ImplementationBridgeHelpers
                .CosmosContainerIdentityHelper.CosmosContainerIdentityAccessor() {
            @Override
            public String getDatabaseName(CosmosContainerIdentity cosmosContainerIdentity) {
                return cosmosContainerIdentity.databaseName;
            }

            @Override
            public String getContainerName(CosmosContainerIdentity cosmosContainerIdentity) {
                return cosmosContainerIdentity.containerName;
            }

            @Override
            public String getContainerLink(CosmosContainerIdentity cosmosContainerIdentity) {
                return cosmosContainerIdentity.containerLink;
            }
        });
    }

    static { initialize(); }
}
