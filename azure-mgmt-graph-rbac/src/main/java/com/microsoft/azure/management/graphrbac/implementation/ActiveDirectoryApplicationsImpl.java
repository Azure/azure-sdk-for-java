/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryApplication;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryApplications;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
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
 * The implementation of Applications and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class ActiveDirectoryApplicationsImpl
        extends CreatableResourcesImpl<
            ActiveDirectoryApplication,
            ActiveDirectoryApplicationImpl,
            ApplicationInner>
        implements
            ActiveDirectoryApplications,
            HasManager<GraphRbacManager>,
            HasInner<ApplicationsInner> {
    private final PagedListConverter<ApplicationInner, ActiveDirectoryApplication> converter;
    private ApplicationsInner innerCollection;
    private GraphRbacManager manager;

    ActiveDirectoryApplicationsImpl(
            final ApplicationsInner client,
            final GraphRbacManager graphRbacManager) {
        this.innerCollection = client;
        this.manager = graphRbacManager;
        converter = new PagedListConverter<ApplicationInner, ActiveDirectoryApplication>() {
            @Override
            public ActiveDirectoryApplication typeConvert(ApplicationInner applicationsInner) {
                ActiveDirectoryApplicationImpl impl = wrapModel(applicationsInner);
                return impl.refreshCredentialsAsync().toBlocking().single();
            }
        };

    }

    @Override
    public PagedList<ActiveDirectoryApplication> list() {
        return wrapList(this.innerCollection.list());
    }

    @Override
    protected PagedList<ActiveDirectoryApplication> wrapList(PagedList<ApplicationInner> pagedList) {
        return converter.convert(pagedList);
    }

    @Override
    public Observable<ActiveDirectoryApplication> listAsync() {
        return wrapPageAsync(this.inner().listAsync())
                .flatMap(new Func1<ActiveDirectoryApplication, Observable<ActiveDirectoryApplication>>() {
                    @Override
                    public Observable<ActiveDirectoryApplication> call(ActiveDirectoryApplication application) {
                        return ((ActiveDirectoryApplicationImpl) application).refreshCredentialsAsync();
                    }
                });
    }

    @Override
    protected ActiveDirectoryApplicationImpl wrapModel(ApplicationInner applicationInner) {
        if (applicationInner == null) {
            return null;
        }
        return new ActiveDirectoryApplicationImpl(applicationInner, manager());
    }

    @Override
    public ActiveDirectoryApplicationImpl getById(String id) {
        return (ActiveDirectoryApplicationImpl) getByIdAsync(id).toBlocking().single();
    }

    @Override
    public Observable<ActiveDirectoryApplication> getByIdAsync(String id) {
        return innerCollection.getAsync(id)
                .map(new Func1<ApplicationInner, ActiveDirectoryApplicationImpl>() {
                    @Override
                    public ActiveDirectoryApplicationImpl call(ApplicationInner applicationInner) {
                        if (applicationInner == null) {
                            return null;
                        }
                        return new ActiveDirectoryApplicationImpl(applicationInner, manager());
                    }
                }).flatMap(new Func1<ActiveDirectoryApplicationImpl, Observable<ActiveDirectoryApplication>>() {
                    @Override
                    public Observable<ActiveDirectoryApplication> call(ActiveDirectoryApplicationImpl application) {
                        return application.refreshCredentialsAsync();
                    }
                });
    }

    @Override
    public ServiceFuture<ActiveDirectoryApplication> getByIdAsync(String id, ServiceCallback<ActiveDirectoryApplication> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
    }

    @Override
    public ActiveDirectoryApplication getByName(String spn) {
        return getByNameAsync(spn).toBlocking().single();
    }

    @Override
    public Observable<ActiveDirectoryApplication> getByNameAsync(final String name) {
        return innerCollection.listWithServiceResponseAsync(String.format("displayName eq '%s'", name))
                .flatMap(new Func1<ServiceResponse<Page<ApplicationInner>>, Observable<Page<ApplicationInner>>>() {
                    @Override
                    public Observable<Page<ApplicationInner>> call(ServiceResponse<Page<ApplicationInner>> result) {
                        if (result == null || result.body().items() == null || result.body().items().isEmpty()) {
                            return innerCollection.listAsync(String.format("appId eq '%s'", name));
                        }
                        return Observable.just(result.body());
                    }
                }).map(new Func1<Page<ApplicationInner>, ActiveDirectoryApplicationImpl>() {
                    @Override
                    public ActiveDirectoryApplicationImpl call(Page<ApplicationInner> result) {
                        if (result == null || result.items() == null || result.items().isEmpty()) {
                            return null;
                        }
                        return new ActiveDirectoryApplicationImpl(result.items().get(0), manager());
                    }
                }).flatMap(new Func1<ActiveDirectoryApplicationImpl, Observable<ActiveDirectoryApplication>>() {
                    @Override
                    public Observable<ActiveDirectoryApplication> call(ActiveDirectoryApplicationImpl application) {
                        if (application == null) {
                            return null;
                        }
                        return application.refreshCredentialsAsync();
                    }
                });
    }

    @Override
    public GraphRbacManager manager() {
        return this.manager;
    }

    @Override
    public ApplicationsInner inner() {
        return this.innerCollection;
    }

    @Override
    protected ActiveDirectoryApplicationImpl wrapModel(String name) {
        return new ActiveDirectoryApplicationImpl(new ApplicationInner().withDisplayName(name), manager());
    }

    @Override
    public Completable deleteByIdAsync(String id) {
        return inner().deleteAsync(id).toCompletable();
    }

    @Override
    public ActiveDirectoryApplicationImpl define(String name) {
        return wrapModel(name);
    }
}
