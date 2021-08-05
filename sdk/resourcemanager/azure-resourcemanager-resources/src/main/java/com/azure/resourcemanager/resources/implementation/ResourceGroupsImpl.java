// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ForceDeletionResourceType;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.azure.resourcemanager.resources.fluent.models.ResourceGroupInner;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

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
        return wrapList(manager().serviceClient().getResourceGroups().list());
    }

    @Override
    public PagedIterable<ResourceGroup> listByTag(String tagName, String tagValue) {
        return new PagedIterable<>(this.listByTagAsync(tagName, tagValue));
    }

    @Override
    public PagedFlux<ResourceGroup> listByTagAsync(String tagName, String tagValue) {
        return wrapPageAsync(manager().serviceClient().getResourceGroups()
            .listAsync(ResourceManagerUtils.createOdataFilterForTags(tagName, tagValue), null));
    }

    @Override
    public ResourceGroupImpl getByName(String name) {
        return wrapModel(manager().serviceClient().getResourceGroups().get(name));
    }

    @Override
    public Mono<ResourceGroup> getByNameAsync(String name) {
        return manager().serviceClient().getResourceGroups().getAsync(name).map(this::wrapModel);
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).block();
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name, Collection<ForceDeletionResourceType> forceDeletionResourceTypes) {
        return manager().serviceClient().getResourceGroups()
            .deleteAsync(name, forceDeletionTypes(forceDeletionResourceTypes));
    }

    @Override
    public void deleteByName(String name, Collection<ForceDeletionResourceType> forceDeletionResourceTypes) {
        deleteByNameAsync(name, forceDeletionResourceTypes).block();
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return manager().serviceClient().getResourceGroups().deleteAsync(name);
    }

    @Override
    public ResourceGroupImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public boolean contain(String name) {
        return manager().serviceClient().getResourceGroups().checkExistence(name);
    }

    @Override
    protected ResourceGroupImpl wrapModel(String name) {
        return new ResourceGroupImpl(new ResourceGroupInner(), name, manager().serviceClient());
    }

    @Override
    protected ResourceGroupImpl wrapModel(ResourceGroupInner inner) {
        if (inner == null) {
            return null;
        }
        return new ResourceGroupImpl(inner, inner.name(), manager().serviceClient());
    }

    @Override
    public Accepted<Void> beginDeleteByName(String name) {
        return beginDeleteByName(name, null);
    }


    @Override
    public Accepted<Void> beginDeleteByName(String name, Collection<ForceDeletionResourceType> forceDeletionResourceTypes) {
        return AcceptedImpl.newAccepted(logger,
            this.manager().serviceClient().getHttpPipeline(),
            this.manager().serviceClient().getDefaultPollInterval(),
            () -> this.manager().serviceClient().getResourceGroups()
                .deleteWithResponseAsync(name, forceDeletionTypes(forceDeletionResourceTypes)).block(),
            Function.identity(),
            Void.class,
            null);
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
        return PagedConverter.mapPage(this.manager().serviceClient().getResourceGroups().listAsync(), inner -> wrapModel(inner));
    }

    @Override
    public ResourceManager manager() {
        return resourceManager;
    }

    private static String forceDeletionTypes(Collection<ForceDeletionResourceType> forceDeletionResourceTypes) {
        String typesInStr = null;
        if (forceDeletionResourceTypes != null && !forceDeletionResourceTypes.isEmpty()) {
            typesInStr = forceDeletionResourceTypes.stream()
                .map(ForceDeletionResourceType::toString)
                .collect(Collectors.joining(","));
        }
        return typesInStr;
    }
}
