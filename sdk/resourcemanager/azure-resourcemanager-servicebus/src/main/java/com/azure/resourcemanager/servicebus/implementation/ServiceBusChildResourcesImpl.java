// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.IndependentChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

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
        ManagerT extends Manager<?>,
        ParentT extends Resource & HasResourceGroup>
        extends IndependentChildResourcesImpl<T, ImplT, InnerT, InnerCollectionT, ManagerT, ParentT>
        implements SupportsGettingByName<T>, SupportsListing<T>, SupportsDeletingByName {
    protected ServiceBusChildResourcesImpl(InnerCollectionT innerCollection, ManagerT manager) {
        super(innerCollection, manager);
    }

    @Override
    public Mono<T> getByNameAsync(String name) {
        return getInnerByNameAsync(name)
            .map(this::wrapModel);
    }

    @Override
    public T getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public PagedFlux<T> listAsync() {
        return PagedConverter.mapPage(this.listInnerAsync(),
                this::wrapModel);
    }

    @Override
    public PagedIterable<T> list() {
        return this.wrapList(this.listInner());
    }

    @Override
    public void deleteByName(String name) {
        this.deleteByNameAsync(name).block();
    }

    public Flux<Void> deleteByNameAsync(List<String> names) {
        if (names == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(names)
            .flatMapDelayError(name -> deleteByNameAsync(name), 32, 32);
    }

    protected abstract Mono<InnerT> getInnerByNameAsync(String name);
    protected abstract PagedFlux<InnerT> listInnerAsync();
    protected abstract PagedIterable<InnerT> listInner();
}
