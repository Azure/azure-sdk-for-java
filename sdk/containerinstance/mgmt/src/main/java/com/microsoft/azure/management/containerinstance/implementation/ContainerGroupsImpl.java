/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.containerinstance.implementation;


import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerinstance.CachedImages;
import com.microsoft.azure.management.containerinstance.Capabilities;
import com.microsoft.azure.management.containerinstance.ContainerGroup;
import com.microsoft.azure.management.containerinstance.ContainerGroups;
import com.microsoft.azure.management.containerinstance.Operation;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation for ContainerGroups.
 */
@LangDefinition
public class ContainerGroupsImpl
        extends
        TopLevelModifiableResourcesImpl<
                ContainerGroup,
                ContainerGroupImpl,
                ContainerGroupInner,
                ContainerGroupsInner,
                ContainerInstanceManager>
        implements ContainerGroups {

    private final StorageManager storageManager;
    private final GraphRbacManager rbacManager;

    protected ContainerGroupsImpl(final ContainerInstanceManager manager, final StorageManager storageManager, final GraphRbacManager rbacManager) {
        super(manager.inner().containerGroups(), manager);
        this.storageManager = storageManager;
        this.rbacManager = rbacManager;
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
