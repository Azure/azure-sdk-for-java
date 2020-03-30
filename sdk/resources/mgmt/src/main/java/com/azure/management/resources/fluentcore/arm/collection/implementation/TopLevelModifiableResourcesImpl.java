/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.resources.fluentcore.arm.collection.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Resource;
import com.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.implementation.ManagerBase;
import com.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.InnerSupportsDelete;
import com.azure.management.resources.fluentcore.collection.InnerSupportsGet;
import com.azure.management.resources.fluentcore.collection.InnerSupportsListing;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.utils.ReactorMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Base class for resource collection classes.
 * (Internal use only)
 *
 * @param <T>                the individual resource type returned
 * @param <ImplT>            the individual resource implementation
 * @param <InnerT>           the wrapper inner type
 * @param <InnerCollectionT> the inner type of the collection object
 * @param <ManagerT>         the manager type for this resource provider type
 */
public abstract class TopLevelModifiableResourcesImpl<
        T extends GroupableResource<ManagerT, InnerT>,
        ImplT extends T,
        InnerT extends Resource,
        InnerCollectionT extends InnerSupportsListing<InnerT> & InnerSupportsGet<InnerT> & InnerSupportsDelete<?>,
        ManagerT extends ManagerBase>
        extends GroupableResourcesImpl<T, ImplT, InnerT, InnerCollectionT, ManagerT>
        implements
        SupportsGettingById<T>,
        SupportsGettingByResourceGroup<T>,
        SupportsDeletingByResourceGroup,
        HasManager<ManagerT>,
        HasInner<InnerCollectionT>,
        SupportsListing<T>,
        SupportsListingByResourceGroup<T>,
        SupportsBatchDeletion {

    protected TopLevelModifiableResourcesImpl(InnerCollectionT innerCollection, ManagerT manager) {
        super(innerCollection, manager);
    }

    @Override
    protected final Mono<InnerT> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    protected Mono<?> deleteInnerAsync(String resourceGroupName, String name) {
        return inner().deleteAsync(resourceGroupName, name);
    }

    @Override
    public Flux<String> deleteByIdsAsync(String... ids) {
        return this.deleteByIdsAsync(new ArrayList<String>(Arrays.asList(ids)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Flux<String> deleteByIdsAsync(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }

        Collection<Mono<String>> observables = new ArrayList<>();
        for (String id : ids) {
            final String resourceGroupName = ResourceUtils.groupFromResourceId(id);
            final String name = ResourceUtils.nameFromResourceId(id);
            Mono<String> o = ReactorMapper.map(this.inner().deleteAsync(resourceGroupName, name), id);
            observables.add(o);
        }

        return Flux.mergeDelayError(32, observables.toArray(new Mono[0]));
    }

    @Override
    public void deleteByIds(String... ids) {
        this.deleteByIds(new ArrayList<String>(Arrays.asList(ids)));
    }

    @Override
    public void deleteByIds(Collection<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.deleteByIdsAsync(ids).blockLast();
        }
    }

    @Override
    public PagedFlux<T> listAsync() {
        return wrapPageAsync(inner().listAsync());
    }

    @Override
    public PagedFlux<T> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(inner().listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    public PagedIterable<T> list() {
        return new PagedIterable<>(this.listAsync());
    }

    @Override
    public PagedIterable<T> listByResourceGroup(String resourceGroupName) {
        return new PagedIterable<>(this.listByResourceGroupAsync(resourceGroupName));
    }
}
