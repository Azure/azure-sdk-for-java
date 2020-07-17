/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.resourcemanager.containerinstance.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.containerinstance.ContainerInstanceManager;
import com.azure.resourcemanager.containerinstance.fluent.ContainerGroupsClient;
import com.azure.resourcemanager.containerinstance.fluent.inner.CachedImagesListResultInner;
import com.azure.resourcemanager.containerinstance.fluent.inner.CapabilitiesListResultInner;
import com.azure.resourcemanager.containerinstance.fluent.inner.ContainerGroupInner;
import com.azure.resourcemanager.containerinstance.fluent.inner.LogsInner;
import com.azure.resourcemanager.containerinstance.fluent.inner.OperationListResultInner;
import com.azure.resourcemanager.containerinstance.models.CachedImages;
import com.azure.resourcemanager.containerinstance.models.Capabilities;
import com.azure.resourcemanager.containerinstance.models.ContainerGroup;
import com.azure.resourcemanager.containerinstance.models.ContainerGroups;
import com.azure.resourcemanager.containerinstance.models.Operation;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedList;
import com.azure.resourcemanager.storage.StorageManager;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Implementation for ContainerGroups.
 */
public class ContainerGroupsImpl
    extends TopLevelModifiableResourcesImpl<
        ContainerGroup,
        ContainerGroupImpl,
        ContainerGroupInner,
        ContainerGroupsClient,
        ContainerInstanceManager>
    implements ContainerGroups {

    public ContainerGroupsImpl(final ContainerInstanceManager manager) {
        super(manager.inner().getContainerGroups(), manager);
    }

    @Override
    protected ContainerGroupImpl wrapModel(String name) {
        return new ContainerGroupImpl(
            name, new ContainerGroupInner(), this.manager());
    }

    @Override
    protected ContainerGroupImpl wrapModel(ContainerGroupInner inner) {
        if (inner == null) {
            return null;
        }
        return new ContainerGroupImpl(
            inner.name(), inner, this.manager());
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String resourceGroupName, String name) {
        return this.manager().inner().getContainerGroups().deleteAsync(resourceGroupName, name).then();
    }

    @Override
    public ContainerGroup.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public String getLogContent(String resourceGroupName, String containerGroupName, String containerName) {
        LogsInner logsInner = this.manager().inner().getContainers().listLogs(resourceGroupName, containerGroupName, containerName);

        return logsInner != null ? logsInner.content() : null;
    }

    @Override
    public String getLogContent(String resourceGroupName, String containerGroupName, String containerName, int tailLineCount) {
        LogsInner logsInner = this.manager().inner().getContainers().listLogs(resourceGroupName, containerGroupName, containerName, tailLineCount);

        return logsInner != null ? logsInner.content() : null;
    }

    @Override
    public Mono<String> getLogContentAsync(String resourceGroupName, String containerGroupName, String containerName) {
        return this.manager().inner().getContainers().listLogsAsync(resourceGroupName, containerGroupName, containerName)
                .map(LogsInner::content);
    }

    @Override
    public Mono<String> getLogContentAsync(String resourceGroupName, String containerGroupName, String containerName, int tailLineCount) {
        return this.manager().inner().getContainers().listLogsAsync(resourceGroupName, containerGroupName, containerName, tailLineCount)
                .map(LogsInner::content);
    }

    @Override
    public PagedIterable<Operation> listOperations() {
        return new PagedIterable<>(listOperationsAsync());
    }

    @Override
    public PagedFlux<Operation> listOperationsAsync() {
        return PagedConverter.convertListToPagedFlux(
            this.manager().inner().getOperations().listAsync()
                .map(OperationListResultInner::value)
        );
    }

    @Override
    public PagedIterable<CachedImages> listCachedImages(String location) {
        return new PagedIterable<>(listCachedImagesAsync(location));
    }

    @Override
    public PagedFlux<CachedImages> listCachedImagesAsync(String location) {
        return PagedConverter.convertListToPagedFlux(
            this.manager().inner().listCachedImagesAsync(location)
                .map(CachedImagesListResultInner::value)
        );
    }

    @Override
    public PagedIterable<Capabilities> listCapabilities(String location) {
        return new PagedIterable<>(listCapabilitiesAsync(location));
    }

    @Override
    public PagedFlux<Capabilities> listCapabilitiesAsync(String location) {
        return PagedConverter.convertListToPagedFlux(
            this.manager().inner().listCapabilitiesAsync(location)
                .map(CapabilitiesListResultInner::value)
        );
    }

    @Override
    public void start(String resourceGroupName, String containerGroupName) {
        this.manager().inner().getContainerGroups().start(resourceGroupName, containerGroupName);
    }

    @Override
    public Mono<Void> startAsync(String resourceGroupName, String containerGroupName) {
        return this.manager().inner().getContainerGroups().startAsync(resourceGroupName, containerGroupName);
    }

    @Override
    public PagedFlux<ContainerGroup> listAsync() {
        return wrapPageAsync(inner().listAsync());
    }

    @Override
    public PagedFlux<ContainerGroup> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(inner().listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    public final PagedIterable<ContainerGroup> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedIterable<ContainerGroup> listByResourceGroup(String resourceGroupName) {
        return new PagedIterable<>(listByResourceGroupAsync(resourceGroupName));
    }
}
