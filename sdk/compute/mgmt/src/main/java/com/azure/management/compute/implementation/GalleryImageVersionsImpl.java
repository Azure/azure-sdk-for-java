/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.compute.models.GalleryImageVersionInner;
import com.azure.management.compute.models.GalleryImageVersionsInner;
import com.azure.management.compute.GalleryImageVersion;
import com.azure.management.compute.GalleryImageVersions;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation for GalleryImageVersions.
 */
class GalleryImageVersionsImpl extends WrapperImpl<GalleryImageVersionsInner> implements GalleryImageVersions {
    private final ComputeManager manager;

    GalleryImageVersionsImpl(ComputeManager manager) {
        super(manager.inner().galleryImageVersions());
        this.manager = manager;
    }

    public ComputeManager manager() {
        return this.manager;
    }

    @Override
    public GalleryImageVersionImpl define(String name) {
        return wrapModel(name);
    }

    private GalleryImageVersionImpl wrapModel(GalleryImageVersionInner inner) {
        return new GalleryImageVersionImpl(inner, manager());
    }

    private GalleryImageVersionImpl wrapModel(String name) {
        return new GalleryImageVersionImpl(name, this.manager());
    }

    @Override
    public PagedFlux<GalleryImageVersion> listByGalleryImageAsync(final String resourceGroupName, final String galleryName, final String galleryImageName) {
        return inner().listByGalleryImageAsync(resourceGroupName, galleryName, galleryImageName)
                .mapPage(this::wrapModel);
    }

    @Override
    public PagedIterable<GalleryImageVersion> listByGalleryImage(String resourceGroupName, String galleryName, String galleryImageName) {
        return inner().listByGalleryImage(resourceGroupName, galleryName, galleryImageName)
                .mapPage(this::wrapModel);
    }

    @Override
    public Mono<GalleryImageVersion> getByGalleryImageAsync(String resourceGroupName, String galleryName, String galleryImageName, String galleryImageVersionName) {
        return inner().getAsync(resourceGroupName, galleryName, galleryImageName, galleryImageVersionName)
                .onErrorResume(e -> Mono.empty())
                .map(this::wrapModel);
    }

    @Override
    public GalleryImageVersion getByGalleryImage(String resourceGroupName, String galleryName, String galleryImageName, String galleryImageVersionName) {
        return this.getByGalleryImageAsync(resourceGroupName, galleryName, galleryImageName, galleryImageVersionName).block();
    }

    @Override
    public Mono<Void> deleteByGalleryImageAsync(String resourceGroupName, String galleryName, String galleryImageName, String galleryImageVersionName) {
        return inner().deleteAsync(resourceGroupName, galleryName, galleryImageName, galleryImageVersionName);
    }

    @Override
    public void deleteByGalleryImage(String resourceGroupName, String galleryName, String galleryImageName, String galleryImageVersionName) {
        this.deleteByGalleryImageAsync(resourceGroupName, galleryName, galleryImageName, galleryImageVersionName).block();
    }
}
