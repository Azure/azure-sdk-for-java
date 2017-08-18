/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.containerinstance.implementation;


import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerinstance.ContainerGroup;
import com.microsoft.azure.management.containerinstance.ContainerGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Completable;
import rx.Observable;

/**
 * Implementation for ContainerGroups.
 */
@LangDefinition
public class ContainerGroupsImpl
    extends
    GroupableResourcesImpl<
            ContainerGroup,
            ContainerGroupImpl,
            ContainerGroupInner,
            ContainerGroupsInner,
            ContainerInstanceManager>
    implements ContainerGroups {

    protected ContainerGroupsImpl(final ContainerInstanceManager manager) {
        super(manager.inner().containerGroups(), manager);
    }

    @Override
    protected ContainerGroupImpl wrapModel(String name) {
        return null;
    }

    @Override
    protected ContainerGroupImpl wrapModel(ContainerGroupInner inner) {
        return null;
    }

    @Override
    protected Observable<ContainerGroupInner> getInnerAsync(String resourceGroupName, String name) {
        return null;
    }

    @Override
    protected Completable deleteInnerAsync(String resourceGroupName, String name) {
        return null;
    }

    @Override
    public PagedList<ContainerGroup> list() {
        return null;
    }

    @Override
    public Observable<ContainerGroup> listAsync() {
        return null;
    }

    @Override
    public PagedList<ContainerGroup> listByResourceGroup(String resourceGroupName) {
        return null;
    }

    @Override
    public Observable<ContainerGroup> listByResourceGroupAsync(String resourceGroupName) {
        return null;
    }

    @Override
    public ContainerGroup.DefinitionStages.Blank define(String name) {
        return null;
    }
}
