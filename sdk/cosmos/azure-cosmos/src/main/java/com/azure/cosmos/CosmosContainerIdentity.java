package com.azure.cosmos;

import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.Utils;

public final class CosmosContainerIdentity {

    private final String databaseName;
    private final String containerName;
    private final String containerLink;


    public CosmosContainerIdentity(String databaseName, String containerName) {

        if (databaseName == null || databaseName.isEmpty()) {
            throw new IllegalArgumentException("databaseName is null or empty");
        }

        if (containerName == null || containerName.isEmpty()) {
            throw new IllegalArgumentException("containerName is null or empty");
        }

        this.containerName = containerName;
        this.databaseName = databaseName;

        String databaseLink = Utils.joinPath(Paths.DATABASES_ROOT, databaseName);
        this.containerLink = Utils.joinPath(databaseLink, Paths.COLLECTIONS_PATH_SEGMENT) + containerName;
    }

    public CosmosContainerIdentity(String containerLink) {

        if (containerLink == null || containerLink.isEmpty()) {
            throw new IllegalArgumentException("containerLink is null or empty");
        }

        this.containerLink = containerLink;
        // TODO: Extract databaseName and containerName
        this.databaseName = "";
        this.containerName = "";
    }

    public String getContainerName() {
        return containerName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getContainerLink() {
        return containerLink;
    }
}
