/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.microsoft.azure.management.resources.ResourceGroup;

import java.io.IOException;

/**
 * The implementation for {@link ResourceGroups} and its parent interfaces.
 */
final class ResourceGroupsImpl
        extends CreatableResourcesImpl<ResourceGroup, ResourceGroupImpl, ResourceGroupInner>
        implements ResourceGroups {
    private final ResourceGroupsInner client;
    private final ResourceManagementClientImpl serviceClient;

    /**
     * Creates an instance of the implementation.
     *
     * @param serviceClient the inner resource management client
     */
    ResourceGroupsImpl(final ResourceManagementClientImpl serviceClient) {
        this.serviceClient = serviceClient;
        this.client = serviceClient.resourceGroups();
    }

    @Override
    public PagedList<ResourceGroup> list() throws CloudException, IOException {
        return wrapList(client.list());
    }

    @Override
    public ResourceGroupImpl getByName(String name) throws CloudException, IOException {
        return wrapModel(client.get(name));
    }

    @Override
    public void delete(String name) throws Exception {
        client.delete(name);
    }

    @Override
    public ResourceGroupImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public boolean checkExistence(String name) throws CloudException, IOException {
        return client.checkExistence(name);
    }

    @Override
    protected ResourceGroupImpl wrapModel(String name) {
        return new ResourceGroupImpl(
                new ResourceGroupInner().withName(name),
                serviceClient);
    }

    @Override
    protected ResourceGroupImpl wrapModel(ResourceGroupInner inner) {
        return new ResourceGroupImpl(inner, serviceClient);
    }
}
