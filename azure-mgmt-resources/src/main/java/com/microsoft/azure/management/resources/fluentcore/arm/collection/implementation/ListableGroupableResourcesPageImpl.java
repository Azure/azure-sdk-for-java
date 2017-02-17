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
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import rx.Observable;

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
public abstract class ListableGroupableResourcesPageImpl<
        T extends GroupableResource<ManagerT, InnerT>,
        ImplT extends T,
        InnerT extends Resource,
        InnerCollectionT,
        ManagerT extends ManagerBase>
        extends ListableGroupableResourcesListImpl<T, ImplT, InnerT, InnerCollectionT, ManagerT>
        implements
        SupportsGettingById<T>,
        SupportsGettingByGroup<T>,
        SupportsDeletingByGroup,
        HasManager<ManagerT>,
        HasInner<InnerCollectionT> {

    protected ListableGroupableResourcesPageImpl(InnerCollectionT innerCollection, ManagerT manager) {
        super(innerCollection, manager);
    }

    protected abstract Observable<Page<InnerT>> listAsyncPage();
    protected abstract Observable<Page<InnerT>> listByGroupAsyncPage(String resourceGroupName);

    protected Observable<List<InnerT>> listInnerAsync() {
        return convertPageToListAsync(listAsyncPage());
    }

    protected Observable<List<InnerT>> listInnerByGroupAsync(String resourceGroupName) {
        return convertPageToListAsync(listByGroupAsyncPage(resourceGroupName));
    }
}
