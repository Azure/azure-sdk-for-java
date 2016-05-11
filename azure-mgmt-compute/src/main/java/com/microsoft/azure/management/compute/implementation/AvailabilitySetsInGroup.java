package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.resources.ResourceGroup;

import java.io.IOException;

public class AvailabilitySetsInGroup implements AvailabilitySets.InGroup {
    private final ResourceGroup resourceGroup;
    private final AvailabilitySets availabilitySets;

    public AvailabilitySetsInGroup(AvailabilitySets availabilitySets, ResourceGroup resourceGroup) {
        this.resourceGroup = resourceGroup;
        this.availabilitySets = availabilitySets;
    }

    @Override
    public AvailabilitySet.DefinitionProvisionable define(String name) {
        return this.availabilitySets
                .define(name)
                .withRegion(this.resourceGroup.location())
                .withExistingGroup(this.resourceGroup.name());
    }

    @Override
    public void delete(String name) throws Exception {
        this.availabilitySets.delete(this.resourceGroup.name(), name);
    }

    @Override
    public PagedList<AvailabilitySet> list() throws CloudException, IOException {
        return this.availabilitySets.list(resourceGroup.name());
    }
}
