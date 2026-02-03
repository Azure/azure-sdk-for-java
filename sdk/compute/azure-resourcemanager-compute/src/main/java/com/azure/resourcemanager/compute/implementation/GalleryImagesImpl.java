// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.GalleryImage;
import com.azure.resourcemanager.compute.models.GalleryImages;
import com.azure.resourcemanager.compute.fluent.models.GalleryImageInner;
import com.azure.resourcemanager.compute.fluent.GalleryImagesClient;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation for GalleryImages. */
public class GalleryImagesImpl extends WrapperImpl<GalleryImagesClient> implements GalleryImages {
    private final ComputeManager manager;

    public GalleryImagesImpl(ComputeManager manager) {
        super(manager.serviceClient().getGalleryImages());
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
        return PagedConverter.mapPage(innerModel().listByGalleryAsync(resourceGroupName, galleryName), this::wrapModel);
    }

    @Override
    public PagedIterable<GalleryImage> listByGallery(String resourceGroupName, String galleryName) {
        return PagedConverter.mapPage(innerModel().listByGallery(resourceGroupName, galleryName), this::wrapModel);
    }

    @Override
    public Mono<GalleryImage> getByGalleryAsync(String resourceGroupName, String galleryName, String galleryImageName) {
        return innerModel().getAsync(resourceGroupName, galleryName, galleryImageName).map(this::wrapModel);
    }

    @Override
    public GalleryImage getByGallery(String resourceGroupName, String galleryName, String galleryImageName) {
        return this.getByGalleryAsync(resourceGroupName, galleryName, galleryImageName).block();
    }

    @Override
    public Mono<Void> deleteByGalleryAsync(String resourceGroupName, String galleryName, String galleryImageName) {
        return innerModel().deleteAsync(resourceGroupName, galleryName, galleryImageName);
    }

    @Override
    public void deleteByGallery(String resourceGroupName, String galleryName, String galleryImageName) {
        this.deleteByGalleryAsync(resourceGroupName, galleryName, galleryImageName).block();
    }
}
