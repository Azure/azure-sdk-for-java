package com.azure.cosmos;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ConnectionConfigBuilder {
    private Set<String>  containerLinks = new HashSet<>();
    private Set<String> preferredRegions = new HashSet<>();


    public ConnectionConfigBuilder addPreferredRegions(Set<String> preferredRegions) {
        this.preferredRegions.addAll(preferredRegions);
        return this;
    }

    public ConnectionConfigBuilder addContainerLinks(Set<String> containerLinks) {
        this.containerLinks.addAll(containerLinks);
        return this;
    }

    public ConnectionConfig build() {
        return new ConnectionConfig(
                this.containerLinks,
                this.preferredRegions
        );
    }

    public ConnectionConfig buildEmptyConfig() {
        return new ConnectionConfig(
                new HashSet<>(),
                new HashSet<>()
        );
    }
}
