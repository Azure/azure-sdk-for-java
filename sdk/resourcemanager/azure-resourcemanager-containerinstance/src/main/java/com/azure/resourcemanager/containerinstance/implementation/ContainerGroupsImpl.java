// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.containerinstance.ContainerInstanceManager;
import com.azure.resourcemanager.containerinstance.fluent.ContainerGroupsClient;
import com.azure.resourcemanager.containerinstance.fluent.models.ContainerGroupInner;
import com.azure.resourcemanager.containerinstance.fluent.models.LogsInner;
import com.azure.resourcemanager.containerinstance.models.CachedImages;
import com.azure.resourcemanager.containerinstance.models.Capabilities;
import com.azure.resourcemanager.containerinstance.models.ContainerAttachResult;
import com.azure.resourcemanager.containerinstance.models.ContainerGroup;
import com.azure.resourcemanager.containerinstance.models.ContainerGroups;
import com.azure.resourcemanager.containerinstance.models.Operation;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import reactor.core.publisher.Mono;

/** Implementation for ContainerGroups. */
public class ContainerGroupsImpl
    extends TopLevelModifiableResourcesImpl<
        ContainerGroup, ContainerGroupImpl, ContainerGroupInner, ContainerGroupsClient, ContainerInstanceManager>
    implements ContainerGroups {

    public ContainerGroupsImpl(final ContainerInstanceManager manager) {
        super(manager.serviceClient().getContainerGroups(), manager);
    }

    @Override
    protected ContainerGroupImpl wrapModel(String name) {
        return new ContainerGroupImpl(name, new ContainerGroupInner(), this.manager());
    }

    @Override
    protected ContainerGroupImpl wrapModel(ContainerGroupInner inner) {
        if (inner == null) {
            return null;
        }
        return new ContainerGroupImpl(inner.name(), inner, this.manager());
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String resourceGroupName, String name) {
        return this.manager().serviceClient().getContainerGroups().deleteAsync(resourceGroupName, name).then();
    }

    @Override
    public ContainerGroup.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public String getLogContent(String resourceGroupName, String containerGroupName, String containerName) {
        LogsInner logsInner = this.manager().serviceClient().getContainers()
            .listLogs(resourceGroupName, containerGroupName, containerName);

        return logsInner != null ? logsInner.content() : null;
    }

    @Override
    public String getLogContent(
        String resourceGroupName, String containerGroupName, String containerName, int tailLineCount) {
        LogsInner logsInner =
            this
                .manager()
                .serviceClient()
                .getContainers()
                .listLogsWithResponse(resourceGroupName, containerGroupName, containerName, tailLineCount, null,
                    Context.NONE)
                .getValue();

        return logsInner != null ? logsInner.content() : null;
    }

    @Override
    public Mono<String> getLogContentAsync(String resourceGroupName, String containerGroupName, String containerName) {
        return this
            .manager()
            .serviceClient()
            .getContainers()
            .listLogsAsync(resourceGroupName, containerGroupName, containerName)
            .map(LogsInner::content);
    }

    @Override
    public Mono<String> getLogContentAsync(
        String resourceGroupName, String containerGroupName, String containerName, int tailLineCount) {
        return this
            .manager()
            .serviceClient()
            .getContainers()
            .listLogsAsync(resourceGroupName, containerGroupName, containerName, tailLineCount, null)
            .map(LogsInner::content);
    }

    @Override
    public PagedIterable<Operation> listOperations() {
        return new PagedIterable<>(listOperationsAsync());
    }

    @Override
    public PagedFlux<Operation> listOperationsAsync() {
        return this.manager().serviceClient().getOperations().listAsync();
    }

    @Override
    public PagedIterable<CachedImages> listCachedImages(String location) {
        return new PagedIterable<>(listCachedImagesAsync(location));
    }

    @Override
    public PagedFlux<CachedImages> listCachedImagesAsync(String location) {
        return this.manager().serviceClient().getLocations().listCachedImagesAsync(location);
    }

    @Override
    public PagedIterable<Capabilities> listCapabilities(String location) {
        return new PagedIterable<>(listCapabilitiesAsync(location));
    }

    @Override
    public PagedFlux<Capabilities> listCapabilitiesAsync(String location) {
        return this.manager().serviceClient().getLocations().listCapabilitiesAsync(location);
    }

    @Override
    public void start(String resourceGroupName, String containerGroupName) {
        this.manager().serviceClient().getContainerGroups().start(resourceGroupName, containerGroupName);
    }

    @Override
    public Mono<Void> startAsync(String resourceGroupName, String containerGroupName) {
        return this.manager().serviceClient().getContainerGroups().startAsync(resourceGroupName, containerGroupName);
    }

    @Override
    public ContainerAttachResult attachOutputStream(String resourceGroupName, String containerGroupName, String containerName) {
        return this.attachOutputStreamAsync(resourceGroupName, containerGroupName, containerName).block();
    }

    @Override
    public Mono<ContainerAttachResult> attachOutputStreamAsync(String resourceGroupName, String containerGroupName, String containerName) {
        return this.manager().serviceClient().getContainers()
            .attachAsync(resourceGroupName, containerGroupName, containerName)
            .map(ContainerAttachResultImpl::new);
    }

    @Override
    public PagedFlux<ContainerGroup> listAsync() {
        return wrapPageAsync(inner().listAsync());
    }

    @Override
    public PagedFlux<ContainerGroup> listByResourceGroupAsync(String resourceGroupName) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return new PagedFlux<>(() -> Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null.")));
        }
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
