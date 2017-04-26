/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByNameAsync;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.ManagerBase;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for Service Bus child entities.
 * Note: When we refactor 'IndependentChildResourcesImpl', move features of this type
 * to 'IndependentChildResourcesImpl' and remove this type.
 *
 * @param <T> the model interface type
 * @param <ImplT> the model interface implementation
 * @param <InnerT> the inner model
 * @param <InnerCollectionT> the inner collection
 * @param <ManagerT> the manager
 * @param <ParentT> the parent model interface type
 */
abstract class ServiceBusChildResourcesImpl<
        T extends IndependentChildResource<ManagerT, InnerT>,
        ImplT extends T,
        InnerT,
        InnerCollectionT,
        ManagerT extends ManagerBase,
        ParentT extends Resource & HasResourceGroup>
        extends IndependentChildResourcesImpl<T, ImplT, InnerT, InnerCollectionT, ManagerT, ParentT>
        implements SupportsGettingByNameAsync<T>, SupportsListing<T>, SupportsDeletingByName {
    protected ServiceBusChildResourcesImpl(InnerCollectionT innerCollection, ManagerT manager) {
        super(innerCollection, manager);
    }

    @Override
    public Observable<T> getByNameAsync(String name) {
        return getInnerByNameAsync(name)
                .map(new Func1<InnerT, T>() {
                    @Override
                    public T call(InnerT inner) {
                        return wrapModel(inner);
                    }
                });
    }

    @Override
    public T getByName(String name) {
        return getByNameAsync(name).toBlocking().last();
    }

    @Override
    public Observable<T> listAsync() {
        return this.listInnerAsync()
                .flatMap(new Func1<ServiceResponse<Page<InnerT>>, Observable<T>>() {
                    @Override
                    public Observable<T> call(ServiceResponse<Page<InnerT>> r) {
                        return Observable.from(r.body().items()).map(new Func1<InnerT, T>() {
                            @Override
                            public T call(InnerT inner) {
                                return wrapModel(inner);
                            }
                        });
                    }
                });
    }

    @Override
    public PagedList<T> list() {
        return this.wrapList(this.listInner());
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).await();
    }

    public Observable<String> deleteByNameAsync(List<String> names) {
        List<Observable<String>> items = new ArrayList<>();
        for (final String name : names) {
            items.add(this.deleteByNameAsync(name).<String>toObservable().map(new Func1<String, String>() {
                @Override
                public String call(String s) {
                    return name;
                }
            }));
        }
        return Observable.mergeDelayError(items);
    }

    protected abstract Observable<InnerT> getInnerByNameAsync(String name);
    protected abstract Observable<ServiceResponse<Page<InnerT>>> listInnerAsync();
    protected abstract PagedList<InnerT> listInner();
}