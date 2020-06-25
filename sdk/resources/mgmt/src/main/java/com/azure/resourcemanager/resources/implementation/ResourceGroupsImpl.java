// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.resources.fluent.inner.ResourceGroupInner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * The implementation for ResourceGroups.
 */
public final class ResourceGroupsImpl
        extends CreatableResourcesImpl<ResourceGroup, ResourceGroupImpl, ResourceGroupInner>
        implements ResourceGroups {

    private final ClientLogger logger = new ClientLogger(ResourceGroupsImpl.class);

    private final ResourceManager resourceManager;

    public ResourceGroupsImpl(final ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public PagedIterable<ResourceGroup> list() {
        return wrapList(manager().inner().getResourceGroups().list());
    }

    @Override
    public PagedIterable<ResourceGroup> listByTag(String tagName, String tagValue) {
        return wrapList(manager().inner().getResourceGroups()
            .list(Utils.createOdataFilterForTags(tagName, tagValue), null));
    }

    @Override
    public PagedFlux<ResourceGroup> listByTagAsync(String tagName, String tagValue) {
        return wrapPageAsync(manager().inner().getResourceGroups()
            .listAsync(Utils.createOdataFilterForTags(tagName, tagValue), null));
    }

    @Override
    public ResourceGroupImpl getByName(String name) {
        return wrapModel(manager().inner().getResourceGroups().get(name));
    }

    @Override
    public Mono<ResourceGroup> getByNameAsync(String name) {
        return manager().inner().getResourceGroups().getAsync(name).map(this::wrapModel);
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).block();
    }


    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return manager().inner().getResourceGroups().deleteAsync(name);
    }

    @Override
    public ResourceGroupImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public boolean contain(String name) {
        return manager().inner().getResourceGroups().checkExistence(name);
    }

    @Override
    protected ResourceGroupImpl wrapModel(String name) {
        return new ResourceGroupImpl(new ResourceGroupInner(), name, manager().inner());
    }

    @Override
    protected ResourceGroupImpl wrapModel(ResourceGroupInner inner) {
        if (inner == null) {
            return null;
        }
        return new ResourceGroupImpl(inner, inner.name(), manager().inner());
    }

    @Override
    public Accepted<Void> beginDeleteByName(String name) {
        Response<Flux<ByteBuffer>> activationResponse = manager().inner().getResourceGroups()
            .deleteWithResponseAsync(name).block();
        if (activationResponse == null) {
            throw logger.logExceptionAsError(new NullPointerException());
        } else {
            return new AcceptedImpl<Void, Void>(activationResponse,
                manager().inner().getSerializerAdapter(),
                manager().inner().getHttpPipeline(),
                Void.class,
                Void.class,
                Function.identity());
        }
    }

//    @Override
//    public Mono<Void> beginDeleteByNameAsync(String name) {
//        // DELETE
//        return client.beginDeleteWithoutPollingAsync(name);
//    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return deleteByNameAsync(ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public PagedFlux<ResourceGroup> listAsync() {
        return this.manager().inner().getResourceGroups().listAsync().mapPage(inner -> wrapModel(inner));
    }

    @Override
    public ResourceManager manager() {
        return resourceManager;
    }
}
