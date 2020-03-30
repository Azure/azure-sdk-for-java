/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerservice.implementation;


import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.containerservice.ContainerService;
import com.azure.management.containerservice.ContainerServices;
import com.azure.management.containerservice.models.ContainerServiceInner;
import com.azure.management.containerservice.models.ContainerServicesInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation for ContainerServices.
 */
public class ContainerServicesImpl extends
        GroupableResourcesImpl<
                        ContainerService,
                        ContainerServiceImpl,
                        ContainerServiceInner,
                        ContainerServicesInner,
                        ContainerServiceManager>
        implements ContainerServices {

    ContainerServicesImpl(final ContainerServiceManager containerServiceManager) {
        super(containerServiceManager.inner().containerServices(), containerServiceManager);
    }

    @Override
    public PagedIterable<ContainerService> list() {
        return new PagedIterable<>(this.listAsync());
    }

    @Override
    public PagedFlux<ContainerService> listAsync() {
        return this.inner().listAsync()
                .mapPage(inner -> new ContainerServiceImpl(inner.getName(), inner, manager()));
    }

    @Override
    public PagedFlux<ContainerService> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
    }


    @Override
    public PagedIterable<ContainerService> listByResourceGroup(String groupName) {
        return wrapList(this.inner().listByResourceGroup(groupName));
    }

    @Override
    protected Mono<ContainerServiceInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    public ContainerServiceImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name);
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

        return new ContainerServiceImpl(containerServiceInner.getName(),
                containerServiceInner,
                this.manager());
    }
}
