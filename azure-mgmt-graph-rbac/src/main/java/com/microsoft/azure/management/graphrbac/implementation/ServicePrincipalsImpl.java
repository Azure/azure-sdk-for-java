/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.graphrbac.GraphErrorException;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.graphrbac.ServicePrincipals;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;
import java.util.List;

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
    public PagedList<ServicePrincipal> list() throws GraphErrorException, IOException {
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
    public ServicePrincipalImpl getByObjectId(String objectId) throws GraphErrorException, IOException {
        return new ServicePrincipalImpl(innerCollection.get(objectId).getBody(), innerCollection);
    }

    @Override
    public ServicePrincipal getByAppId(String appId) throws GraphErrorException, IOException {
        return null;
    }

    @Override
    public ServicePrincipal getByServicePrincipalName(String spn) throws GraphErrorException, IOException {
        List<ServicePrincipalInner> spList = innerCollection.list(String.format("servicePrincipalNames/any(c:c eq '%s')", spn)).getBody();
        if (spList == null || spList.isEmpty()) {
            return null;
        } else {
            return new ServicePrincipalImpl(spList.get(0), innerCollection);
        }
    }

    @Override
    public ServiceCall<ServicePrincipal> getByServicePrincipalNameAsync(final String spn, final ServiceCallback<ServicePrincipal> callback) {
        final ServiceCall<ServicePrincipal> serviceCall = new ServiceCall<>(null);
        serviceCall.newCall(innerCollection.listAsync(String.format("servicePrincipalNames/any(c:c eq '%s')", spn), new ListOperationCallback<ServicePrincipalInner>() {
            @Override
            public void failure(Throwable t) {
                callback.failure(t);
                serviceCall.failure(t);
            }

            @Override
            public void success(ServiceResponse<List<ServicePrincipalInner>> result) {
                List<ServicePrincipalInner> servicePrincipals = result.getBody();
                if (servicePrincipals == null || servicePrincipals.isEmpty()) {
                    failure(new GraphErrorException("Service principal not found for SPN: " + spn));
                }
                ServicePrincipal user = new ServicePrincipalImpl(servicePrincipals.get(0), innerCollection);
                ServiceResponse<ServicePrincipal> clientResponse = new ServiceResponse<>(user, result.getResponse());
                callback.success(clientResponse);
                serviceCall.success(clientResponse);
            }
        }).getCall());
        return serviceCall;
    }
}
