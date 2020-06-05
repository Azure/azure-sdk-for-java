// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.resources.fluent.inner.ResourceGroupInner;
import com.azure.resourcemanager.resources.fluent.ResourceGroupsClient;
import com.azure.resourcemanager.resources.ResourceManagementClient;
import reactor.core.publisher.Mono;

/**
 * The implementation for ResourceGroups.
 */
public final class ResourceGroupsImpl
        extends CreatableResourcesImpl<ResourceGroup, ResourceGroupImpl, ResourceGroupInner>
        implements ResourceGroups {
    private final ResourceGroupsClient client;
    private final ResourceManagementClient serviceClient;

    /**
     * Creates an instance of the implementation.
     *
     * @param serviceClient the inner resource management client
     */
    public ResourceGroupsImpl(final ResourceManagementClient serviceClient) {
        this.serviceClient = serviceClient;
        this.client = serviceClient.getResourceGroups();
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
        return new ResourceGroupImpl(inner, inner.name(), serviceClient);
    }

    @Override
    public void beginDeleteByName(String id) {
        beginDeleteByNameAsync(id).block();
    }

    @Override
    public Mono<Void> beginDeleteByNameAsync(String name) {
        // DELETE
        return client.beginDeleteWithoutPollingAsync(name);
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
