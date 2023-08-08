// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.GenericResources;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.models.ResourcesMoveInfo;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.fluent.models.GenericResourceInner;
import com.azure.resourcemanager.resources.fluent.ResourcesClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/**
 * Implementation of the {@link GenericResources}.
 */
public final class GenericResourcesImpl
        extends GroupableResourcesImpl<
        GenericResource,
        GenericResourceImpl,
        GenericResourceInner,
    ResourcesClient,
    ResourceManager>
        implements GenericResources {
    private final ClientLogger logger = new ClientLogger(getClass());

    public GenericResourcesImpl(ResourceManager resourceManager) {
        super(resourceManager.serviceClient().getResources(), resourceManager);
    }

    @Override
    public PagedIterable<GenericResource> list() {
        return new PagedIterable<>(this.listAsync());
    }

    @Override
    public PagedIterable<GenericResource> listByResourceGroup(String groupName) {
        return new PagedIterable<>(this.listByResourceGroupAsync(groupName));
    }

    @Override
    public PagedIterable<GenericResource> listByTag(String resourceGroupName, String tagName, String tagValue) {
        return new PagedIterable<>(this.listByTagAsync(resourceGroupName, tagName, tagValue));
    }

    @Override
    public PagedFlux<GenericResource> listByTagAsync(String resourceGroupName, String tagName, String tagValue) {
        return wrapPageAsync(PagedConverter.mapPage(this.manager().serviceClient().getResources()
                .listByResourceGroupAsync(resourceGroupName,
                    ResourceManagerUtils.createOdataFilterForTags(tagName, tagValue), null, null),
                res -> (GenericResourceInner) res));
    }

    @Override
    public GenericResource.DefinitionStages.Blank define(String name) {
        return new GenericResourceImpl(
                name,
                new GenericResourceInner(),
                this.manager());
    }

    @Override
    public boolean checkExistence(String resourceGroupName, String resourceProviderNamespace,
            String parentResourcePath, String resourceType, String resourceName, String apiVersion) {
        return this.inner().checkExistence(
                resourceGroupName,
                resourceProviderNamespace,
                parentResourcePath,
                resourceType,
                resourceName,
                apiVersion);
    }

    @Override
    public boolean checkExistenceById(String id) {
        if (CoreUtils.isNullOrEmpty(id)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Parameter 'id' is required and cannot be null."));
        }
        String apiVersion = getApiVersionFromIdAsync(id).block();
        return this.checkExistenceById(id, apiVersion);
    }

    @Override
    public boolean checkExistenceById(String id, String apiVersion) {
        if (CoreUtils.isNullOrEmpty(id)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Parameter 'id' is required and cannot be null."));
        }
        if (CoreUtils.isNullOrEmpty(apiVersion)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Parameter 'apiVersion' is required and cannot be null."));
        }
        return this.inner().checkExistenceById(id, apiVersion);
    }

    @Override
    public Mono<GenericResource> getByIdAsync(String id) {
        if (CoreUtils.isNullOrEmpty(id)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'id' is required and cannot be null."));
        }
        return this.getApiVersionFromIdAsync(id)
            .flatMap(apiVersion -> this.getByIdAsync(id, apiVersion));
    }

    @Override
    public GenericResource getById(String id, String apiVersion) {
        return this.getByIdAsync(id, apiVersion).block();
    }

    @Override
    public Mono<GenericResource> getByIdAsync(String id, String apiVersion) {
        if (CoreUtils.isNullOrEmpty(id)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'id' is required and cannot be null."));
        }
        if (CoreUtils.isNullOrEmpty(apiVersion)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'apiVersion' is required and cannot be null."));
        }
        return this.inner().getByIdAsync(id, apiVersion)
            .map(this::wrapModel)
            .map(r -> r.withApiVersion(apiVersion));
    }

    @Override
    public Mono<Void> deleteByIdAsync(final String id) {
        if (CoreUtils.isNullOrEmpty(id)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'id' is required and cannot be null."));
        }
        return getApiVersionFromIdAsync(id)
            .flatMap(apiVersion -> this.deleteByIdAsync(id, apiVersion));
    }

    @Override
    public void deleteById(String id, String apiVersion) {
        this.deleteByIdAsync(id, apiVersion).block();
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id, String apiVersion) {
        if (CoreUtils.isNullOrEmpty(id)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'id' is required and cannot be null."));
        }
        if (CoreUtils.isNullOrEmpty(apiVersion)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'apiVersion' is required and cannot be null."));
        }
        return this.inner().deleteByIdAsync(id, apiVersion);
    }

    @Override
    public GenericResource get(
            String resourceGroupName,
            String providerNamespace,
            String resourceType,
            String name) {

        PagedIterable<GenericResource> genericResources = this.listByResourceGroup(resourceGroupName);
        for (GenericResource resource : genericResources) {
            if (resource.name().equalsIgnoreCase(name)
                    && resource.resourceProviderNamespace().equalsIgnoreCase(providerNamespace)
                    && resource.resourceType().equalsIgnoreCase(resourceType)) {
                return resource;
            }
        }
        return null;
    }

    @Override
    public GenericResource get(
            String resourceGroupName,
            String resourceProviderNamespace,
            String parentResourcePath,
            String resourceType,
            String resourceName,
            String apiVersion) {

        // Correct for auto-gen'd API's treatment parent path as required
        // even though it makes sense only for child resources
        if (parentResourcePath == null) {
            parentResourcePath = "";
        }

        GenericResourceInner inner = this.inner().get(
                resourceGroupName,
                resourceProviderNamespace,
                parentResourcePath,
                resourceType,
                resourceName,
                apiVersion);

        GenericResourceImpl resource = new GenericResourceImpl(
                resourceName,
                inner,
                this.manager());

        return resource.withExistingResourceGroup(resourceGroupName)
                .withProviderNamespace(resourceProviderNamespace)
                .withParentResourcePath(parentResourcePath)
                .withResourceType(resourceType)
                .withApiVersion(apiVersion);
    }

    @Override
    public void moveResources(String sourceResourceGroupName,
            ResourceGroup targetResourceGroup, List<String> resources) {
        this.moveResourcesAsync(sourceResourceGroupName, targetResourceGroup, resources).block();
    }

    @Override
    public Mono<Void> moveResourcesAsync(String sourceResourceGroupName,
            ResourceGroup targetResourceGroup, List<String> resources) {
        ResourcesMoveInfo moveInfo = new ResourcesMoveInfo();
        moveInfo.withTargetResourceGroup(targetResourceGroup.id());
        moveInfo.withResources(resources);
        return this.inner().moveResourcesAsync(sourceResourceGroupName, moveInfo);
    }

    public void delete(String resourceGroupName, String resourceProviderNamespace,
            String parentResourcePath, String resourceType, String resourceName, String apiVersion) {
        deleteAsync(resourceGroupName, resourceProviderNamespace,
            parentResourcePath, resourceType, resourceName, apiVersion).block();
    }

    @Override
    public Mono<Void> deleteAsync(String resourceGroupName, String resourceProviderNamespace,
            String parentResourcePath, String resourceType, String resourceName, String apiVersion) {
        return this.inner().deleteAsync(resourceGroupName, resourceProviderNamespace,
            parentResourcePath, resourceType, resourceName, apiVersion);
    }

    @Override
    protected GenericResourceImpl wrapModel(String id) {
        return new GenericResourceImpl(id, new GenericResourceInner(), this.manager())
                .withExistingResourceGroup(ResourceUtils.groupFromResourceId(id))
                .withProviderNamespace(ResourceUtils.resourceProviderFromResourceId(id))
                .withResourceType(ResourceUtils.resourceTypeFromResourceId(id))
                .withParentResourceId(ResourceUtils.parentResourceIdFromResourceId(id));
    }

    @Override
    protected GenericResourceImpl wrapModel(GenericResourceInner inner) {
        if (inner == null) {
            return null;
        }
        return new GenericResourceImpl(inner.id(), inner, this.manager())
                .withExistingResourceGroup(ResourceUtils.groupFromResourceId(inner.id()))
                .withProviderNamespace(ResourceUtils.resourceProviderFromResourceId(inner.id()))
                .withResourceType(ResourceUtils.resourceTypeFromResourceId(inner.id()))
                .withParentResourceId(ResourceUtils.parentResourceIdFromResourceId(inner.id()));
    }

    @Override
    public Mono<GenericResourceInner> getInnerAsync(String groupName, String name) {
        // Not needed, can't be supported, provided only to satisfy GroupableResourceImpl's requirements
        throw logger.logExceptionAsError(new UnsupportedOperationException(
            "Get just by resource group and name is not supported. Please use other overloads."));
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String resourceGroupName, String name) {
        // Not needed, can't be supported, provided only to satisfy GroupableResourceImpl's requirements
        throw logger.logExceptionAsError(new UnsupportedOperationException(
            "Delete just by resource group and name is not supported. Please use other overloads."));
    }

    @Override
    public Accepted<Void> beginDeleteById(String id) {
        if (CoreUtils.isNullOrEmpty(id)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Parameter 'id' is required and cannot be null."));
        }
        String apiVersion = getApiVersionFromIdAsync(id).block();
        return this.beginDeleteById(id, apiVersion);
    }

    @Override
    public Accepted<Void> beginDeleteById(String id, String apiVersion) {
        if (CoreUtils.isNullOrEmpty(id)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Parameter 'id' is required and cannot be null."));
        }
        if (CoreUtils.isNullOrEmpty(apiVersion)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Parameter 'apiVersion' is required and cannot be null."));
        }
        return AcceptedImpl.newAccepted(logger,
            this.manager().serviceClient().getHttpPipeline(),
            this.manager().serviceClient().getDefaultPollInterval(),
            () -> this.inner().deleteByIdWithResponseAsync(id, apiVersion).block(),
            Function.identity(),
            Void.class,
            null,
            Context.NONE);
    }

    private Mono<String> getApiVersionFromIdAsync(final String id) {
        return this.manager().providers().getByNameAsync(ResourceUtils.resourceProviderFromResourceId(id))
                .map(provider -> ResourceUtils.defaultApiVersion(id, provider));
    }

    @Override
    public PagedFlux<GenericResource> listAsync() {
        return wrapPageAsync(PagedConverter.mapPage(this.inner().listAsync(),
                res -> (GenericResourceInner) res));
    }

    @Override
    public PagedFlux<GenericResource> listByResourceGroupAsync(String resourceGroupName) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return new PagedFlux<>(() -> Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null.")));
        }
        return wrapPageAsync(PagedConverter.mapPage(this.manager().serviceClient().getResources()
                .listByResourceGroupAsync(resourceGroupName),
            res -> (GenericResourceInner) res));
    }
}
