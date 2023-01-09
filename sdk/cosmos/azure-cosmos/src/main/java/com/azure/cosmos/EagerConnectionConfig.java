package com.azure.cosmos;

import java.util.List;

public final class EagerConnectionConfig {
    private final List<CosmosContainerIdentity> cosmosContainerIdentities;
    private final List<String> eagerConnectionRegions;

    EagerConnectionConfig(List<CosmosContainerIdentity> cosmosContainerIdentities, List<String> eagerConnectionRegions) {
        this.cosmosContainerIdentities = cosmosContainerIdentities;
        this.eagerConnectionRegions = eagerConnectionRegions;
    }

    public List<CosmosContainerIdentity> getCosmosContainerIdentities() {
        return cosmosContainerIdentities;
    }

    public List<String> getEagerConnectionRegions() {
        return eagerConnectionRegions;
    }
}
