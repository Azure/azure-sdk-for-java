/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.resources.ResourceGroup;

import java.io.IOException;

public class PublicIpAddressesInGroupImpl implements PublicIpAddresses.InGroup {
    private final ResourceGroup resourceGroup;
    private final PublicIpAddresses publicIpAddresses;

    public PublicIpAddressesInGroupImpl(PublicIpAddresses publicIpAddresses, ResourceGroup resourceGroup) {
        this.resourceGroup = resourceGroup;
        this.publicIpAddresses = publicIpAddresses;
    }

    @Override
    public PublicIpAddress.DefinitionAfterGroup define(String name) {
        return this.publicIpAddresses
                .define(name)
                .withRegion(this.resourceGroup.location())
                .withExistingGroup(this.resourceGroup.name());
    }

    @Override
    public void delete(String name) throws Exception {
        this.publicIpAddresses.delete(this.resourceGroup.name(), name);
    }

    @Override
    public PagedList<PublicIpAddress> list() throws CloudException, IOException {
        return this.publicIpAddresses.list(resourceGroup.name());
    }
}
