/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.GraphErrorException;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.graphrbac.ServicePrincipals;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * The implementation of ServicePrincipals and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
class ServicePrincipalsImpl
        extends ReadableWrappersImpl<
                    ServicePrincipal,
                    ServicePrincipalImpl,
                    ServicePrincipalInner>
        implements
            ServicePrincipals,
            HasManager<GraphRbacManager>,
            HasInner<ServicePrincipalsInner> {
    private ServicePrincipalsInner innerCollection;
    private GraphRbacManager manager;

    ServicePrincipalsImpl(
            final ServicePrincipalsInner client,
            final GraphRbacManager graphRbacManager) {
        this.innerCollection = client;
        this.manager = graphRbacManager;
    }

    @Override
    public PagedList<ServicePrincipal> list() {
        return wrapList(this.innerCollection.list());
    }

    @Override
    public Observable<ServicePrincipal> listAsync() {
        return wrapPageAsync(this.inner().listAsync());
    }

    @Override
    protected ServicePrincipalImpl wrapModel(ServicePrincipalInner servicePrincipalInner) {
        if (servicePrincipalInner == null) {
            return null;
        }
        return new ServicePrincipalImpl(servicePrincipalInner, this.innerCollection);
    }

    @Override
    public ServicePrincipalImpl getByObjectId(String objectId) {
        return new ServicePrincipalImpl(innerCollection.get(objectId), innerCollection);
    }

    @Override
    public ServicePrincipal getByAppId(String appId) {
        return null;
    }

    @Override
    public ServicePrincipal getByServicePrincipalName(String spn) {
        List<ServicePrincipalInner> spList = innerCollection.list(String.format("servicePrincipalNames/any(c:c eq '%s')", spn));
        if (spList == null || spList.isEmpty()) {
            return null;
        } else {
            return new ServicePrincipalImpl(spList.get(0), innerCollection);
        }
    }

    @Override
    public ServiceFuture<ServicePrincipal> getByServicePrincipalNameAsync(final String spn, final ServiceCallback<ServicePrincipal> callback) {
        return ServiceFuture.fromBody(getByServicePrincipalNameAsync(spn), callback);
    }

    @Override
    public Observable<ServicePrincipal> getByServicePrincipalNameAsync(final String spn) {
        return innerCollection.listWithServiceResponseAsync(String.format("servicePrincipalNames/any(c:c eq '%s')", spn))
                .map(new Func1<ServiceResponse<Page<ServicePrincipalInner>>, ServicePrincipal>() {
                    @Override
                    public ServicePrincipal call(ServiceResponse<Page<ServicePrincipalInner>> result) {
                        if (result == null || result.body().items() == null || result.body().items().isEmpty()) {
                            throw new GraphErrorException("Service principal not found for SPN: " + spn, result.response());
                        }
                        return new ServicePrincipalImpl(result.body().items().get(0), innerCollection);
                    }
                });
    }

    @Override
    public GraphRbacManager manager() {
        return this.manager;
    }

    @Override
    public ServicePrincipalsInner inner() {
        return this.innerCollection;
    }
}
