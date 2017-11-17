/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.containerinstance.implementation;


import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerinstance.ContainerGroup;
import com.microsoft.azure.management.containerinstance.ContainerGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

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

    protected ContainerGroupsImpl(final ContainerInstanceManager manager, final StorageManager storageManager) {
        super(manager.inner().containerGroups(), manager);
        this.storageManager = storageManager;
    }

    @Override
    protected ContainerGroupImpl wrapModel(String name) {
        return new ContainerGroupImpl(name, new ContainerGroupInner(), this.manager(), this.storageManager);
    }

    @Override
    protected ContainerGroupImpl wrapModel(ContainerGroupInner inner) {
        if (inner == null) {
            return null;
        }
        return new ContainerGroupImpl(inner.name(), inner, this.manager(), this.storageManager);
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
    public String getLogContent(String resourceGroupName, String containerName, String containerGroupName) {
        return this.manager().inner().containerLogs().list(resourceGroupName, containerName, containerGroupName).content();
    }

    @Override
    public String getLogContent(String resourceGroupName, String containerName, String containerGroupName, int tailLineCount) {
        return this.manager().inner().containerLogs().list(resourceGroupName, containerName, containerGroupName, tailLineCount).content();
    }

    @Override
    public Observable<String> getLogContentAsync(String resourceGroupName, String containerName, String containerGroupName) {
        return this.manager().inner().containerLogs().listAsync(resourceGroupName, containerName, containerGroupName)
            .map(new Func1<LogsInner, String>() {
                @Override
                public String call(LogsInner logsInner) {
                    return logsInner.content();
                }
            });
    }

    @Override
    public Observable<String> getLogContentAsync(String resourceGroupName, String containerName, String containerGroupName, int tailLineCount) {
        return this.manager().inner().containerLogs().listAsync(resourceGroupName, containerName, containerGroupName, tailLineCount)
            .map(new Func1<LogsInner, String>() {
                @Override
                public String call(LogsInner logsInner) {
                    return logsInner.content();
                }
            });
    }
}
