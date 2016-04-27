package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.resources.ResourceGroup;

import java.io.IOException;
import java.util.List;

public class AvailabilitySetsInGroup implements AvailabilitySets.InGroup {
    private final ResourceGroup resourceGroup;
    private final AvailabilitySets availabilitySetsCore;

    public AvailabilitySetsInGroup(AvailabilitySets availabilitySetsCore, ResourceGroup resourceGroup) {
        this.resourceGroup = resourceGroup;
        this.availabilitySetsCore = availabilitySetsCore;
    }

    @Override
    public AvailabilitySet.DefinitionProvisionable define(String name) throws Exception {
        return this.availabilitySetsCore
                .define(name)
                .withRegion(this.resourceGroup.location())
                .withExistingGroup(this.resourceGroup.name());
    }

    @Override
    public void delete(String name) throws Exception {
        this.availabilitySetsCore.delete(this.resourceGroup.name(), name);
    }

    @Override
    public PagedList<AvailabilitySet> list() throws CloudException, IOException {
        return this.availabilitySetsCore.list(resourceGroup.name());
    }
}
