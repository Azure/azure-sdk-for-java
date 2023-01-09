package com.azure.cosmos;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class EagerConnectionConfig {
    private final List<String> containerLinks;
    private final List<String> eagerConnectionRegions;

    EagerConnectionConfig(List<String> containerLinks, List<String> eagerConnectionRegions) {
        this.containerLinks = containerLinks;
        this.eagerConnectionRegions = eagerConnectionRegions;
    }

    public List<String> getContainerLinks() {
        return containerLinks;
    }

    public List<String> getEagerConnectionRegions() {
        return eagerConnectionRegions;
    }
}
