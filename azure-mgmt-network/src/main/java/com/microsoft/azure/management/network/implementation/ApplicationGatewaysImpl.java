/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGateways;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;

import java.io.IOException;

/**
 *  Implementation for {@link ApplicationGateways}.
 */
class ApplicationGatewaysImpl
        extends GroupableResourcesImpl<
        ApplicationGateway,
        ApplicationGatewayImpl,
        ApplicationGatewayInner,
        ApplicationGatewaysInner,
        NetworkManager>
        implements ApplicationGateways {

    ApplicationGatewaysImpl(
            final NetworkManagementClientImpl networkClient,
            final NetworkManager networkManager) {
        super(networkClient.applicationGateways(), networkManager);
    }

    @Override
    public PagedList<ApplicationGateway> list() throws CloudException, IOException {
        return wrapList(this.innerCollection.listAll().getBody());
    }

    @Override
    public PagedList<ApplicationGateway> listByGroup(String groupName) throws CloudException, IOException {
        return wrapList(this.innerCollection.list(groupName).getBody());
    }

    @Override
    public ApplicationGatewayImpl getByGroup(String groupName, String name) throws CloudException, IOException {
        return wrapModel(this.innerCollection.get(groupName, name).getBody());
    }

    @Override
    public void delete(String id) throws Exception {
        delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.innerCollection.delete(groupName, name);
    }

    @Override
    public ApplicationGatewayImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected ApplicationGatewayImpl wrapModel(String name) {
        ApplicationGatewayInner inner = new ApplicationGatewayInner();
        return new ApplicationGatewayImpl(
                name,
                inner,
                this.innerCollection,
                super.myManager);
    }

    @Override
    protected ApplicationGatewayImpl wrapModel(ApplicationGatewayInner inner) {
        return new ApplicationGatewayImpl(
                inner.name(),
                inner,
                this.innerCollection,
                this.myManager);
    }
}

