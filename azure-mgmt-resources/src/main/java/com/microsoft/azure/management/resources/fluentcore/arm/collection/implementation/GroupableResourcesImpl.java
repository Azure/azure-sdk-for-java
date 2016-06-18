/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import java.io.IOException;
import java.util.List;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.rest.RestException;

/**
 * Base class for resource collection classes.
 * (Internal use only)
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 * @param <InnerT> the wrapper inner type
 * @param <InnerCollectionT> the inner type of the collection object
 */
public abstract class GroupableResourcesImpl<
        T extends GroupableResource,
        ImplT extends T,
        InnerT extends Resource,
        InnerCollectionT>
    implements
        SupportsGettingById<T>,
        SupportsGettingByGroup<T> {

    protected final ResourceManager resourceManager;
    protected final InnerCollectionT innerCollection;
    private final PagedListConverter<InnerT, T> converter;

    protected GroupableResourcesImpl(
            ResourceManager resourceManager,
            InnerCollectionT innerCollection) {
        this.resourceManager = resourceManager;
        this.innerCollection = innerCollection;
        this.converter = new PagedListConverter<InnerT, T>() {
            @Override
            public T typeConvert(InnerT inner) {
                return wrapModel(inner);
            }
        };
    }

    @Override
    public abstract T getByGroup(String groupName, String name) throws CloudException, IOException;

    @Override
    public final T getById(String id) throws CloudException, IOException {
        return this.getByGroup(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(id));
    }

    protected abstract ImplT wrapModel(String name);

    protected abstract ImplT wrapModel(InnerT inner);

    protected PagedList<T> wrapList(PagedList<InnerT> pagedList) {
        return converter.convert(pagedList);
    }

    protected PagedList<T> wrapList(List<InnerT> list) {
        PageImpl<InnerT> page = new PageImpl<>();
        page.setItems(list);
        page.setNextPageLink(null);
        PagedList<InnerT> pagedList = new PagedList<InnerT>(page) {
            @Override
            public Page<InnerT> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        };

        return converter.convert(pagedList);
    }
}
