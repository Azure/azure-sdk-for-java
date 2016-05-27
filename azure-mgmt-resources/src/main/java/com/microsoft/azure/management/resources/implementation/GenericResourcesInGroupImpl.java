/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceGroup;

import java.io.IOException;

/**
 * The implementation of GenericResources.InGroup and its parent interfaces.
 */
final class GenericResourcesInGroupImpl implements GenericResources.InGroup {
    private final GenericResources genericResources;
    private final ResourceGroup resourceGroup;

    GenericResourcesInGroupImpl(GenericResources genericResources, ResourceGroup resourceGroup) {
        this.genericResources = genericResources;
        this.resourceGroup = resourceGroup;
    }

    @Override
    public GenericResource get(String name) throws CloudException, IOException {
        return this.genericResources.get(this.resourceGroup.name(), name);
    }

    @Override
    public PagedList<GenericResource> list() throws CloudException, IOException {
        return this.genericResources.list(resourceGroup.name());
    }
}
