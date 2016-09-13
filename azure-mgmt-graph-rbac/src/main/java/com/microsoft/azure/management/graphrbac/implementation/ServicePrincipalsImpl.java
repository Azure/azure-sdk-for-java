/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.graphrbac.GraphErrorException;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.graphrbac.ServicePrincipals;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

import java.io.IOException;
import java.util.List;

/**
 * The implementation of ServicePrincipals and its parent interfaces.
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
        return wrapList(this.innerCollection.list());
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
        return new ServicePrincipalImpl(innerCollection.get(objectId), innerCollection);
    }

    @Override
    public ServicePrincipal getByAppId(String appId) throws GraphErrorException, IOException {
        return null;
    }

    @Override
    public ServicePrincipal getByServicePrincipalName(String spn) throws GraphErrorException, IOException {
        List<ServicePrincipalInner> spList = innerCollection.list(String.format("servicePrincipalNames/any(c:c eq '%s')", spn));
        if (spList == null || spList.isEmpty()) {
            return null;
        } else {
            return new ServicePrincipalImpl(spList.get(0), innerCollection);
        }
    }

    @Override
    public ServiceCall<ServicePrincipal> getByServicePrincipalNameAsync(final String spn, final ServiceCallback<ServicePrincipal> callback) {
        return ServiceCall.create(
                getByServicePrincipalNameAsync(spn).map(new Func1<ServicePrincipal, ServiceResponse<ServicePrincipal>>() {
                    @Override
                    public ServiceResponse<ServicePrincipal> call(ServicePrincipal fluentModelT) {
                        return new ServiceResponse<>(fluentModelT, null);
                    }
                }), callback
        );
    }

    @Override
    public Observable<ServicePrincipal> getByServicePrincipalNameAsync(final String spn) {
        return innerCollection.listAsync(String.format("servicePrincipalNames/any(c:c eq '%s')", spn))
                .map(new Func1<Page<ServicePrincipalInner>, ServicePrincipal>() {
                    @Override
                    public ServicePrincipal call(Page<ServicePrincipalInner> result) {
                        if (result == null || result.getItems() == null || result.getItems().isEmpty()) {
                            throw new GraphErrorException("Service principal not found for SPN: " + spn);
                        }
                        return new ServicePrincipalImpl(result.getItems().get(0), innerCollection);
                    }
                });
    }
}
