/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.graphrbac.ServicePrincipals;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation of ServicePrincipals and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class ServicePrincipalsImpl
        extends CreatableWrappersImpl<
            ServicePrincipal,
            ServicePrincipalImpl,
            ServicePrincipalInner>
        implements
            ServicePrincipals,
            HasManager<GraphRbacManager>,
            HasInner<ServicePrincipalsInner> {
    private final PagedListConverter<ServicePrincipalInner, ServicePrincipal> converter;
    private ServicePrincipalsInner innerCollection;
    private GraphRbacManager manager;

    ServicePrincipalsImpl(
            final ServicePrincipalsInner client,
            final GraphRbacManager graphRbacManager) {
        this.innerCollection = client;
        this.manager = graphRbacManager;
        converter = new PagedListConverter<ServicePrincipalInner, ServicePrincipal>() {
            @Override
            public ServicePrincipal typeConvert(ServicePrincipalInner servicePrincipalInner) {
                ServicePrincipalImpl impl = wrapModel(servicePrincipalInner);
                return impl.refreshCredentialsAsync().toBlocking().single();
            }
        };
    }

    @Override
    public PagedList<ServicePrincipal> list() {
        return converter.convert(this.inner().list());
    }

    @Override
    public Observable<ServicePrincipal> listAsync() {
        return wrapPageAsync(this.inner().listAsync())
                .flatMap(new Func1<ServicePrincipal, Observable<ServicePrincipal>>() {
                    @Override
                    public Observable<ServicePrincipal> call(ServicePrincipal servicePrincipal) {
                        return ((ServicePrincipalImpl) servicePrincipal).refreshCredentialsAsync();
                    }
                });
    }

    @Override
    protected ServicePrincipalImpl wrapModel(ServicePrincipalInner servicePrincipalInner) {
        if (servicePrincipalInner == null) {
            return null;
        }
        return new ServicePrincipalImpl(servicePrincipalInner, manager());
    }

    @Override
    public ServicePrincipalImpl getById(String id) {
        return (ServicePrincipalImpl) getByIdAsync(id).toBlocking().single();
    }

    @Override
    public Observable<ServicePrincipal> getByIdAsync(String id) {
        return innerCollection.getAsync(id)
                .map(new Func1<ServicePrincipalInner, ServicePrincipalImpl>() {
                    @Override
                    public ServicePrincipalImpl call(ServicePrincipalInner servicePrincipalInner) {
                        if (servicePrincipalInner == null) {
                            return null;
                        }
                        return new ServicePrincipalImpl(servicePrincipalInner, manager());
                    }
                }).flatMap(new Func1<ServicePrincipalImpl, Observable<ServicePrincipal>>() {
                    @Override
                    public Observable<ServicePrincipal> call(ServicePrincipalImpl servicePrincipal) {
                        if (servicePrincipal == null) {
                            return null;
                        }
                        return servicePrincipal.refreshCredentialsAsync();
                    }
                });
    }

    @Override
    public ServiceFuture<ServicePrincipal> getByIdAsync(String id, ServiceCallback<ServicePrincipal> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
    }

    @Override
    public ServicePrincipal getByName(String spn) {
        return getByNameAsync(spn).toBlocking().single();
    }

    @Override
    public Observable<ServicePrincipal> getByNameAsync(final String name) {
        return innerCollection.listWithServiceResponseAsync(String.format("servicePrincipalNames/any(c:c eq '%s')", name))
                .flatMap(new Func1<ServiceResponse<Page<ServicePrincipalInner>>, Observable<Page<ServicePrincipalInner>>>() {
                    @Override
                    public Observable<Page<ServicePrincipalInner>> call(ServiceResponse<Page<ServicePrincipalInner>> result) {
                        if (result == null || result.body().items() == null || result.body().items().isEmpty()) {
                            return innerCollection.listAsync(String.format("displayName eq '%s'", name));
                        }
                        return Observable.just(result.body());
                    }
                }).map(new Func1<Page<ServicePrincipalInner>, ServicePrincipalImpl>() {
                    @Override
                    public ServicePrincipalImpl call(Page<ServicePrincipalInner> result) {
                        if (result == null || result.items() == null || result.items().isEmpty()) {
                            return null;
                        }
                        return new ServicePrincipalImpl(result.items().get(0), manager());
                    }
                }).flatMap(new Func1<ServicePrincipalImpl, Observable<ServicePrincipal>>() {
                    @Override
                    public Observable<ServicePrincipal> call(ServicePrincipalImpl servicePrincipal) {
                        if (servicePrincipal == null) {
                            return null;
                        }
                        return servicePrincipal.refreshCredentialsAsync();
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

    @Override
    public ServicePrincipalImpl define(String name) {
        return new ServicePrincipalImpl((ServicePrincipalInner) new ServicePrincipalInner().withDisplayName(name), manager());
    }

    @Override
    protected ServicePrincipalImpl wrapModel(String name) {
        return new ServicePrincipalImpl((ServicePrincipalInner) new ServicePrincipalInner().withDisplayName(name), manager());
    }

    @Override
    public Completable deleteByIdAsync(String id) {
        return manager().inner().servicePrincipals().deleteAsync(id).toCompletable();
    }
}
