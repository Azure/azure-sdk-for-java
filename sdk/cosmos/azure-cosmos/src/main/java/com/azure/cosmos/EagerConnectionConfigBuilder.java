package com.azure.cosmos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EagerConnectionConfigBuilder {
    private List<String> containerLinks = new ArrayList<>();
    private List<String> eagerConnectionRegions = new ArrayList<>();


    public EagerConnectionConfigBuilder addPreferredRegions(List<String> eagerConnectionRegions) {
        this.eagerConnectionRegions = eagerConnectionRegions;
        return this;
    }

    public EagerConnectionConfigBuilder addContainerLinks(List<String> containerLinks) {
        this.containerLinks = containerLinks;
        return this;
    }

    public EagerConnectionConfig build() {
        return new EagerConnectionConfig(
                this.containerLinks,
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
