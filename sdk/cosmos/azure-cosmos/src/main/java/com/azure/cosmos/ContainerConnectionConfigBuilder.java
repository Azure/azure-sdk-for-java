package com.azure.cosmos;

import java.util.Collections;
import java.util.Set;

public final class ContainerConnectionConfigBuilder {
    private final String containerLink;
    private Set<String> preferredRegions;
    private int maxChannelsPerReplica;

    public ContainerConnectionConfigBuilder(String containerLink) {
        this.containerLink = containerLink;
    }

    public ContainerConnectionConfigBuilder setPreferredRegions(Set<String> preferredRegions) {
        this.preferredRegions = preferredRegions;
        return this;
    }

    public ContainerConnectionConfigBuilder setMaxChannelsPerReplica(int maxChannelsPerReplica) {
        this.maxChannelsPerReplica = maxChannelsPerReplica;
        return this;
    }

    public ContainerConnectionConfig build() {
        return new ContainerConnectionConfig(
                this.containerLink,
                this.preferredRegions,
                this.maxChannelsPerReplica
        );
    }

    ContainerConnectionConfig buildEmptyConfig() {
        return new ContainerConnectionConfig(
                this.containerLink,
                Collections.emptySet(),
                1
        );
    }

}
