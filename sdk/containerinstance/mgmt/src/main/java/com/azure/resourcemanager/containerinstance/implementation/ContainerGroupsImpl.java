/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.resourcemanager.containerinstance.implementation;

import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.containerinstance.ContainerInstanceManager;
import com.azure.resourcemanager.containerinstance.fluent.ContainerGroupsClient;
import com.azure.resourcemanager.containerinstance.fluent.inner.CachedImagesListResultInner;
import com.azure.resourcemanager.containerinstance.fluent.inner.CapabilitiesListResultInner;
import com.azure.resourcemanager.containerinstance.fluent.inner.ContainerGroupInner;
import com.azure.resourcemanager.containerinstance.fluent.inner.LogsInner;
import com.azure.resourcemanager.containerinstance.models.CachedImages;
import com.azure.resourcemanager.containerinstance.models.Capabilities;
import com.azure.resourcemanager.containerinstance.models.ContainerGroup;
import com.azure.resourcemanager.containerinstance.models.ContainerGroups;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedList;
import com.azure.resourcemanager.storage.StorageManager;

import java.util.Collections;
import java.util.List;

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

    private final StorageManager storageManager;
    private final AuthorizationManager authorizationManager;

    public ContainerGroupsImpl(final ContainerInstanceManager manager, final StorageManager storageManager, final AuthorizationManager authorizationManager) {
        super(manager.inner().getContainerGroups(), manager);
        this.storageManager = storageManager;
        this.authorizationManager = authorizationManager;
    }

    @Override
    protected ContainerGroupImpl wrapModel(String name) {
        return new ContainerGroupImpl(name, new ContainerGroupInner(), this.manager(), this.storageManager, this.rbacManager);
    }

    @Override
    protected ContainerGroupImpl wrapModel(ContainerGroupInner inner) {
        if (inner == null) {
            return null;
        }
        return new ContainerGroupImpl(inner.name(), inner, this.manager(), this.storageManager, this.rbacManager);
    }

    @Override
    protected Completable deleteInnerAsync(String resourceGroupName, String name) {
        return this.manager().inner().containerGroups().deleteAsync(resourceGroupName, name).toCompletable();
    }

    @Override
    public ContainerGroup.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public String getLogContent(String resourceGroupName, String containerGroupName, String containerName) {
        LogsInner logsInner = this.manager().inner().containers().listLogs(resourceGroupName, containerGroupName, containerName);

        return logsInner != null ? logsInner.content() : null;
    }

    @Override
    public String getLogContent(String resourceGroupName, String containerGroupName, String containerName, int tailLineCount) {
        LogsInner logsInner = this.manager().inner().containers().listLogs(resourceGroupName, containerGroupName, containerName, tailLineCount);

        return logsInner != null ? logsInner.content() : null;
    }

    @Override
    public Observable<String> getLogContentAsync(String resourceGroupName, String containerGroupName, String containerName) {
        return this.manager().inner().containers().listLogsAsync(resourceGroupName, containerGroupName, containerName)
                .map(new Func1<LogsInner, String>() {
                    @Override
                    public String call(LogsInner logsInner) {
                        return logsInner.content();
                    }
                });
    }

    @Override
    public Observable<String> getLogContentAsync(String resourceGroupName, String containerGroupName, String containerName, int tailLineCount) {
        return this.manager().inner().containers().listLogsAsync(resourceGroupName, containerGroupName, containerName, tailLineCount)
                .map(new Func1<LogsInner, String>() {
                    @Override
                    public String call(LogsInner logsInner) {
                        return logsInner.content();
                    }
                });
    }

    @Override
    public Set<Operation> listOperations() {
        OperationListResultInner operationListResultInner = this.manager().inner().operations().list();

        return Collections.unmodifiableSet(operationListResultInner != null && operationListResultInner.value() != null
                ? new HashSet<Operation>(operationListResultInner.value())
                : new HashSet<Operation>());
    }

    @Override
    public Observable<Set<Operation>> listOperationsAsync() {
        return this.manager().inner().operations().listAsync()
                .map(new Func1<OperationListResultInner, Set<Operation>>() {
                    @Override
                    public Set<Operation> call(OperationListResultInner operationListResultInner) {
                        return Collections.unmodifiableSet(operationListResultInner != null && operationListResultInner.value() != null
                                ? new HashSet<Operation>(operationListResultInner.value())
                                : new HashSet<Operation>());
                    }
                });
    }

    @Override
    public List<CachedImages> listCachedImages(String location) {
        return this.manager().inner().listCachedImages(location).value();
    }

    @Override
    public Observable<CachedImages> listCachedImagesAsync(String location) {
        return this.manager().inner().listCachedImagesAsync(location)
                .flatMap(new Func1<CachedImagesListResultInner, Observable<CachedImages>>() {
                    @Override
                    public Observable<CachedImages> call(CachedImagesListResultInner cachedImagesListResultInner) {
                        return Observable.from(cachedImagesListResultInner.value());
                    }
                });
    }

    @Override
    public List<Capabilities> listCapabilities(String location) {
        return this.manager().inner().listCapabilities(location).value();
    }

    @Override
    public Observable<Capabilities> listCapabilitiesAsync(String location) {
        return this.manager().inner().listCapabilitiesAsync(location)
                .flatMap(new Func1<CapabilitiesListResultInner, Observable<Capabilities>>() {
                    @Override
                    public Observable<Capabilities> call(CapabilitiesListResultInner capabilitiesListResultInner) {
                        return Observable.from(capabilitiesListResultInner.value());
                    }
                });
    }

    @Override
    public void start(String resourceGroupName, String containerGroupName) {
        this.manager().inner().containerGroups().start(resourceGroupName, containerGroupName);
    }

    @Override
    public Completable startAsync(String resourceGroupName, String containerGroupName) {
        return this.manager().inner().containerGroups().startAsync(resourceGroupName, containerGroupName).toCompletable();
    }

    @Override
    public Observable<ContainerGroup> listAsync() {
        return wrapPageAsync(inner().listAsync())
                .flatMap(new Func1<ContainerGroup, Observable<ContainerGroup>>() {
                    @Override
                    public Observable<ContainerGroup> call(ContainerGroup containerGroup) {
                        return containerGroup.refreshAsync();
                    }
                });
    }

    @Override
    public Observable<ContainerGroup> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(inner().listByResourceGroupAsync(resourceGroupName))
                .flatMap(new Func1<ContainerGroup, Observable<ContainerGroup>>() {
                    @Override
                    public Observable<ContainerGroup> call(ContainerGroup containerGroup) {
                        return containerGroup.refreshAsync();
                    }
                });
    }

    @Override
    public final PagedList<ContainerGroup> list() {
        final PagedListConverter<ContainerGroupInner, ContainerGroup> converter = new PagedListConverter<ContainerGroupInner, ContainerGroup>() {
            @Override
            public Observable<ContainerGroup> typeConvertAsync(ContainerGroupInner inner) {
                return wrapModel(inner).refreshAsync();
            }
        };
        return converter.convert(this.inner().list());
    }

    @Override
    public PagedList<ContainerGroup> listByResourceGroup(String resourceGroupName) {
        final PagedListConverter<ContainerGroupInner, ContainerGroup> converter = new PagedListConverter<ContainerGroupInner, ContainerGroup>() {
            @Override
            public Observable<ContainerGroup> typeConvertAsync(ContainerGroupInner inner) {
                return wrapModel(inner).refreshAsync();
            }
        };
        return converter.convert(this.inner().listByResourceGroup(resourceGroupName));
    }
}
