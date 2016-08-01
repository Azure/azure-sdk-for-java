/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.graphrbac.ServicePrincipals;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;

import java.io.IOException;

/**
 * The implementation of StorageAccounts and its parent interfaces.
 */
class ServicePrincipalsImpl
        extends CreatableWrappersImpl<
            ServicePrincipal,
            ServicePrincipalImpl,
            ServicePrincipalInner>
        implements ServicePrincipals {
    private ServicePrincipalsInner innerCollection;
    private GraphRbacManager manager;

    ServicePrincipalsImpl(
            final ServicePrincipalsInner client,
            final GraphRbacManager graphRbacManager) {
        this.innerCollection = client;
        this.manager = graphRbacManager;
    }

    @Override
    public PagedList<ServicePrincipal> list() throws CloudException, IOException {
        return wrapList(this.innerCollection.list().getBody());
    }

    @Override
    public void delete(String id) throws Exception {
        innerCollection.delete(id);
    }

    @Override
    public ServicePrincipalImpl define(String appId) {
        return wrapModel(appId);
    }

    @Override
    protected ServicePrincipalImpl wrapModel(String appId) {
        return new ServicePrincipalImpl(appId, innerCollection);
    }

    @Override
    protected ServicePrincipalImpl wrapModel(ServicePrincipalInner servicePrincipalInner) {
        return new ServicePrincipalImpl(servicePrincipalInner, this.innerCollection);
    }

    @Override
    public ServicePrincipalImpl getByName(String objectId) throws CloudException, IOException {
        return new ServicePrincipalImpl(innerCollection.get(objectId).getBody(), innerCollection);
    }
}
