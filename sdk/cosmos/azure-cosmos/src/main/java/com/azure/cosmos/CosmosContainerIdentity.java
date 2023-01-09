package com.azure.cosmos;

import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.Utils;

public final class CosmosContainerIdentity {

    private final String databaseName;
    private final String containerName;
    private final String containerLink;


    public CosmosContainerIdentity(String databaseName, String containerName) {
        this.containerName = containerName;
        this.databaseName = databaseName;

        String databaseLink = Utils.joinPath(Paths.DATABASES_ROOT, databaseName);
        this.containerLink = Utils.joinPath(databaseLink, Paths.COLLECTIONS_PATH_SEGMENT) + containerName;
    }

    public CosmosContainerIdentity(String containerLink) {
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
