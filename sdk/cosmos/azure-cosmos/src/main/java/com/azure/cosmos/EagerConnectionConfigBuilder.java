package com.azure.cosmos;

import java.util.ArrayList;
import java.util.List;

public final class EagerConnectionConfigBuilder {
    private List<CosmosContainerIdentity> cosmosContainerIdentities = new ArrayList<>();
    private List<String> eagerConnectionRegions = new ArrayList<>();


    public EagerConnectionConfigBuilder addEagerConnectionRegions(List<String> eagerConnectionRegions) {
        this.eagerConnectionRegions = eagerConnectionRegions;
        return this;
    }

    public EagerConnectionConfigBuilder addContainerIdentities(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        this.cosmosContainerIdentities = cosmosContainerIdentities;
        return this;
    }

    public EagerConnectionConfig build() {
        return new EagerConnectionConfig(
                this.cosmosContainerIdentities,
                this.eagerConnectionRegions
        );
    }

    public EagerConnectionConfig buildEmptyConfig() {
        return new EagerConnectionConfig(
                new ArrayList<>(),
                new ArrayList<>()
        );
    }
}
