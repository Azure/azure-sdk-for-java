/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.ManagerBase;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsAsyncListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * Base class for resource collection classes.
 * (Internal use only)
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 * @param <InnerT> the wrapper inner type
 * @param <InnerCollectionT> the inner type of the collection object
 * @param <ManagerT> the manager type for this resource provider type
 */
public abstract class ListableGroupableResourcesImpl<
        T extends GroupableResource<ManagerT, InnerT>,
        ImplT extends T,
        InnerT extends Resource,
        InnerCollectionT extends SupportsAsyncListing<Page<InnerT>>,
        ManagerT extends ManagerBase>
        extends GroupableResourcesImpl<T, ImplT, InnerT, InnerCollectionT, ManagerT>
        implements
        SupportsGettingById<T>,
        SupportsGettingByGroup<T>,
        SupportsDeletingByGroup,
        HasManager<ManagerT>,
        HasInner<InnerCollectionT>,
        SupportsAsyncListing<T> {

    protected ListableGroupableResourcesImpl(InnerCollectionT innerCollection, ManagerT manager) {
        super(innerCollection, manager);
    }

    @Override
    public Observable<T> listAsync() {
        return this.innerCollection.listAsync().map(new Func1<Page<InnerT>, List<InnerT>>() {
            @Override
            public List<InnerT> call(Page<InnerT> pageInner) {
                return pageInner.items();
            }
        }).flatMap(new Func1<List<InnerT>, Observable<T>>() {
            @Override
            public Observable<T> call(List<InnerT> inners) {
                return Observable.from(inners).map(new Func1<InnerT, T>() {
                    @Override
                    public T call(InnerT inner) {
                        return wrapModel(inner);
                    }
                });
            }
        });
    }
}
