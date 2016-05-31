/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.resources.ResourceGroup;

import java.io.IOException;

public class AvailabilitySetsInGroupImpl 
    implements AvailabilitySets.InGroup {
    private final ResourceGroup resourceGroup;
    private final AvailabilitySets availabilitySets;

    public AvailabilitySetsInGroupImpl(AvailabilitySets availabilitySets, ResourceGroup resourceGroup) {
        this.resourceGroup = resourceGroup;
        this.availabilitySets = availabilitySets;
    }


    @Override
    public void delete(String name) throws Exception {
        this.availabilitySets.delete(this.resourceGroup.name(), name);
    }

    @Override
    public PagedList<AvailabilitySet> list() throws CloudException, IOException {
        return this.availabilitySets.list(resourceGroup.name());
    }


    @Override
    public AvailabilitySet.DefinitionAfterGroup define(String name) {
        return this.availabilitySets
                .define(name)
                .withRegion(this.resourceGroup.location())
                .withExistingGroup(this.resourceGroup.name());
    }


    @Override
    public AvailabilitySet get(String name) throws CloudException, IOException {
        return this.availabilitySets.get(resourceGroup.name(), name);
    }
}
