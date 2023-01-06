package com.azure.cosmos;

import java.util.Collections;
import java.util.Set;

public final class ConnectionConfig {
    private final Set<String> containerLinks;
    private final Set<String> preferredRegions;

    ConnectionConfig(Set<String> containerLinks, Set<String> preferredRegions) {
        this.containerLinks = containerLinks;
        this.preferredRegions = preferredRegions;
    }

    public Set<String> getContainerLinks() {
        return Collections.unmodifiableSet(containerLinks);
    }

    public Set<String> getPreferredRegions() {
        return Collections.unmodifiableSet(preferredRegions);
    }
}
