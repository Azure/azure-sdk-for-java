package com.azure.cosmos;

import java.util.Collections;
import java.util.Set;

public final class ContainerConnectionConfig {
    private final String containerLink;
    private final Set<String> preferredRegions;
    private final int maxChannelsPerReplica;

    ContainerConnectionConfig(String containerLink, Set<String> preferredRegions, int maxChannelsPerReplica) {
        this.containerLink = containerLink;
        this.preferredRegions = preferredRegions;
        this.maxChannelsPerReplica = maxChannelsPerReplica;
    }

    public String getContainerLink() {
        return containerLink;
    }

    public Set<String> getPreferredRegions() {
        return Collections.unmodifiableSet(preferredRegions);
    }

    public int getMaxChannelsPerReplica() {
        return maxChannelsPerReplica;
    }
}
