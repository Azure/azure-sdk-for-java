// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.Galleries;
import com.azure.resourcemanager.compute.models.Gallery;
import com.azure.resourcemanager.compute.fluent.GalleriesClient;
import com.azure.resourcemanager.compute.fluent.models.GalleryInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.BatchDeletionImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** The implementation for Galleries. */
public class GalleriesImpl
    extends GroupableResourcesImpl<Gallery, GalleryImpl, GalleryInner, GalleriesClient, ComputeManager>
    implements Galleries {
    public GalleriesImpl(ComputeManager manager) {
        super(manager.serviceClient().getGalleries(), manager);
    }

    @Override
    protected Mono<GalleryInner> getInnerAsync(String resourceGroupName, String name) {
        return inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String resourceGroupName, String name) {
        return inner().deleteAsync(resourceGroupName, name);
    }

    @Override
    public Flux<String> deleteByIdsAsync(Collection<String> ids) {
        return BatchDeletionImpl.deleteByIdsAsync(ids, this::deleteInnerAsync);
    }

    @Override
    public Flux<String> deleteByIdsAsync(String... ids) {
        return this.deleteByIdsAsync(new ArrayList<>(Arrays.asList(ids)));
    }

    @Override
    public void deleteByIds(Collection<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.deleteByIdsAsync(ids).last().block();
        }
    }

    @Override
    public void deleteByIds(String... ids) {
        this.deleteByIds(new ArrayList<>(Arrays.asList(ids)));
    }

    @Override
    public PagedIterable<Gallery> listByResourceGroup(String resourceGroupName) {
        return this.wrapList(inner().listByResourceGroup(resourceGroupName));
    }

    @Override
    public PagedFlux<Gallery> listByResourceGroupAsync(String resourceGroupName) {
        return inner().listByResourceGroupAsync(resourceGroupName).mapPage(this::wrapModel);
    }

    @Override
    public PagedIterable<Gallery> list() {
        return this.wrapList(inner().list());
    }

    @Override
    public PagedFlux<Gallery> listAsync() {
        return inner().listAsync().mapPage(this::wrapModel);
    }

    @Override
    public GalleryImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected GalleryImpl wrapModel(GalleryInner inner) {
        return new GalleryImpl(inner.name(), inner, manager());
    }

    @Override
    protected GalleryImpl wrapModel(String name) {
        return new GalleryImpl(name, new GalleryInner(), this.manager());
    }
}
