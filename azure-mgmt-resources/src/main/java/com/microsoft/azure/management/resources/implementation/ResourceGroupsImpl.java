/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupsInner;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;

import java.io.IOException;

/**
 * The implementation for ResourceGroups and its parent interfaces.
 */
final class ResourceGroupsImpl
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
        PagedListConverter<ResourceGroupInner, ResourceGroup> converter = new PagedListConverter<ResourceGroupInner, ResourceGroup>() {
            @Override
            public ResourceGroup typeConvert(ResourceGroupInner resourceGroupInner) {
                return createFluentModel(resourceGroupInner);
            }
        };
        return converter.convert(client.list().getBody());
    }

    @Override
    public ResourceGroupImpl get(String name) throws CloudException, IOException {
        ResourceGroupInner resourceGroupInner = client.get(name).getBody();
        return createFluentModel(resourceGroupInner);
    }

    @Override
    public void delete(String name) throws Exception {
        client.delete(name);
    }

    @Override
    public ResourceGroupImpl define(String name) {
        return createFluentModel(name);
    }

    @Override
    public boolean checkExistence(String name) throws CloudException, IOException {
        return client.checkExistence(name).getBody();
    }

    private ResourceGroupImpl createFluentModel(String name) {
        ResourceGroupInner resourceGroupInner = new ResourceGroupInner();
        resourceGroupInner.withName(name);
        return new ResourceGroupImpl(resourceGroupInner, serviceClient);
    }

    private ResourceGroupImpl createFluentModel(ResourceGroupInner resourceGroupInner) {
        return new ResourceGroupImpl(resourceGroupInner, serviceClient);
    }
}
