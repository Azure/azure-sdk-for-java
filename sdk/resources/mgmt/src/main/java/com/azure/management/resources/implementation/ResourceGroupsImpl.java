/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.ResourceGroups;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.azure.management.resources.fluentcore.utils.Utils;
import com.azure.management.resources.models.ResourceGroupInner;
import com.azure.management.resources.models.ResourceGroupsInner;
import com.azure.management.resources.models.ResourceManagementClientImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation for ResourceGroups.
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
    public PagedIterable<ResourceGroup> list() {
        return wrapList(client.list());
    }

    @Override
    public PagedIterable<ResourceGroup> listByTag(String tagName, String tagValue) {
        return wrapList(client.list(Utils.createOdataFilterForTags(tagName, tagValue), null));
    }

    @Override
    public PagedFlux<ResourceGroup> listByTagAsync(String tagName, String tagValue) {
        return wrapPageAsync(client.listAsync(Utils.createOdataFilterForTags(tagName, tagValue), null));
    }

    @Override
    public ResourceGroupImpl getByName(String name) {
        return wrapModel(client.get(name));
    }

    @Override
    public Mono<ResourceGroup> getByNameAsync(String name) {
        return client.getAsync(name).map(inner -> wrapModel(inner));
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).block();
    }


    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return client.deleteAsync(name);
    }

    @Override
    public ResourceGroupImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public boolean contain(String name) {
        return client.checkExistence(name);
    }

    @Override
    protected ResourceGroupImpl wrapModel(String name) {
        return new ResourceGroupImpl(new ResourceGroupInner(), name, serviceClient);
    }

    @Override
    protected ResourceGroupImpl wrapModel(ResourceGroupInner inner) {
        if (inner == null) {
            return null;
        }
        return new ResourceGroupImpl(inner, inner.getName(), serviceClient);
    }

    @Override
    public void beginDeleteByName(String id) {
        beginDeleteByNameAsync(id).block();
    }

    @Override
    public Mono<Void> beginDeleteByNameAsync(String name) {
        // DELETE
        return client.beginDeleteAsync(name);
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return deleteByNameAsync(ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public PagedFlux<ResourceGroup> listAsync() {
        return this.client.listAsync().mapPage(inner -> wrapModel(inner));
    }
}
