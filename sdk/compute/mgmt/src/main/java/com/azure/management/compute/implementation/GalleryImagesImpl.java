/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.compute.models.GalleryImageInner;
import com.azure.management.compute.models.GalleryImagesInner;
import com.azure.management.compute.GalleryImage;
import com.azure.management.compute.GalleryImages;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation for GalleryImages.
 */
class GalleryImagesImpl extends WrapperImpl<GalleryImagesInner> implements GalleryImages {
    private final ComputeManager manager;

    GalleryImagesImpl(ComputeManager manager) {
        super(manager.inner().galleryImages());
        this.manager = manager;
    }

    public ComputeManager manager() {
        return this.manager;
    }

    @Override
    public GalleryImageImpl define(String name) {
        return wrapModel(name);
    }

    private GalleryImageImpl wrapModel(GalleryImageInner inner) {
        return new GalleryImageImpl(inner, manager());
    }

    private GalleryImageImpl wrapModel(String name) {
        return new GalleryImageImpl(name, this.manager());
    }

    @Override
    public PagedFlux<GalleryImage> listByGalleryAsync(final String resourceGroupName, final String galleryName) {
        return inner().listByGalleryAsync(resourceGroupName, galleryName)
                .mapPage(this::wrapModel);
    }

    @Override
    public PagedIterable<GalleryImage> listByGallery(String resourceGroupName, String galleryName) {
        return inner().listByGallery(resourceGroupName, galleryName)
                .mapPage(this::wrapModel);
    }

    @Override
    public Mono<GalleryImage> getByGalleryAsync(String resourceGroupName, String galleryName, String galleryImageName) {
        return inner().getAsync(resourceGroupName, galleryName, galleryImageName)
                .onErrorResume(e -> Mono.empty())
                .map(this::wrapModel);
    }

    @Override
    public GalleryImage getByGallery(String resourceGroupName, String galleryName, String galleryImageName) {
        return this.getByGalleryAsync(resourceGroupName, galleryName, galleryImageName).block();
    }

    @Override
    public Mono<Void> deleteByGalleryAsync(String resourceGroupName, String galleryName, String galleryImageName) {
        return inner().deleteAsync(resourceGroupName, galleryName, galleryImageName);
    }

    @Override
    public void deleteByGallery(String resourceGroupName, String galleryName, String galleryImageName) {
        this.deleteByGalleryAsync(resourceGroupName, galleryName, galleryImageName).block();
    }
}
