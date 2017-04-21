/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.ContainerService;
import com.microsoft.azure.management.compute.ContainerServices;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * The implementation for ContainerServices.
 */
@LangDefinition
public class ContainerServicesImpl
        extends
        GroupableResourcesImpl<
                ContainerService,
                ContainerServiceImpl,
                ContainerServiceInner,
                ContainerServicesInner,
                ComputeManager>
        implements ContainerServices {

    ContainerServicesImpl(final ComputeManager computeManager) {
        super(computeManager.inner().containerServices(), computeManager);
    }

    @Override
    public PagedList<ContainerService> list() {
        final ContainerServicesImpl self = this;
        return new GroupPagedList<ContainerService>(this.manager().resourceManager().resourceGroups().list()) {
            @Override
            public List<ContainerService> listNextGroup(String resourceGroupName) {
                return wrapList(self.inner().listByResourceGroup(resourceGroupName));
            }
        };
    }

    @Override
    public Observable<ContainerService> listAsync() {
        return this.manager().resourceManager().resourceGroups().listAsync()
                .flatMap(new Func1<ResourceGroup, Observable<ContainerService>>() {
                    @Override
                    public Observable<ContainerService> call(ResourceGroup resourceGroup) {
                        return wrapPageAsync(inner().listByResourceGroupAsync(resourceGroup.name()));
                    }
                });
    }

    @Override
    public Observable<ContainerService> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
    }


    @Override
    public PagedList<ContainerService> listByResourceGroup(String groupName) {
        return wrapList(this.inner().listByResourceGroup(groupName));
    }

    @Override
    protected Observable<ContainerServiceInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    public ContainerServiceImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected Completable deleteInnerAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name).toCompletable();
    }

    /**************************************************************
     * Fluent model helpers.
     **************************************************************/

    @Override
    protected ContainerServiceImpl wrapModel(String name) {
        return new ContainerServiceImpl(name,
                new ContainerServiceInner(),
                this.manager());
    }

    @Override
    protected ContainerServiceImpl wrapModel(ContainerServiceInner containerServiceInner) {
        if (containerServiceInner == null) {
            return null;
        }

        return new ContainerServiceImpl(containerServiceInner.name(),
                containerServiceInner,
                this.manager());
    }
}
