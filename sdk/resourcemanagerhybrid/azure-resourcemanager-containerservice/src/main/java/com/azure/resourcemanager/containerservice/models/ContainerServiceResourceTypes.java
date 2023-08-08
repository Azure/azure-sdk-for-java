// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerservice.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/** Defines values for resource types from container service. */
public class ContainerServiceResourceTypes extends ExpandableStringEnum<ContainerServiceResourceTypes> {

    /** Static value managedClusters for ContainerServiceResourceTypes. */
    public static final ContainerServiceResourceTypes MANAGED_CLUSTERS = fromString("managedClusters");

    /**
     * Creates or finds a ContainerServiceResourceTypes from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ContainerServiceResourceTypes.
     */
    @JsonCreator
    public static ContainerServiceResourceTypes fromString(String name) {
        return fromString(name, ContainerServiceResourceTypes.class);
    }

    /** @return known ExtendedLocationTypes values. */
    public static Collection<ContainerServiceResourceTypes> values() {
        return values(ContainerServiceResourceTypes.class);
    }
}
