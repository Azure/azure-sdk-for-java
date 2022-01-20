// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.GalleryImageVersion;
import com.azure.resourcemanager.compute.models.GalleryImageVersions;
import com.azure.resourcemanager.compute.fluent.models.GalleryImageVersionInner;
import com.azure.resourcemanager.compute.fluent.GalleryImageVersionsClient;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation for GalleryImageVersions. */
public class GalleryImageVersionsImpl extends WrapperImpl<GalleryImageVersionsClient> implements GalleryImageVersions {
    private final ComputeManager manager;

    public GalleryImageVersionsImpl(ComputeManager manager) {
        super(manager.serviceClient().getGalleryImageVersions());
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
    public PagedFlux<GalleryImageVersion> listByGalleryImageAsync(
        final String resourceGroupName, final String galleryName, final String galleryImageName) {
        return PagedConverter.mapPage(innerModel()
            .listByGalleryImageAsync(resourceGroupName, galleryName, galleryImageName),
            this::wrapModel);
    }

    @Override
    public PagedIterable<GalleryImageVersion> listByGalleryImage(
        String resourceGroupName, String galleryName, String galleryImageName) {
        return PagedConverter.mapPage(innerModel()
            .listByGalleryImage(resourceGroupName, galleryName, galleryImageName),
            this::wrapModel);
    }

    @Override
    public Mono<GalleryImageVersion> getByGalleryImageAsync(
        String resourceGroupName, String galleryName, String galleryImageName, String galleryImageVersionName) {
        return innerModel()
            .getAsync(resourceGroupName, galleryName, galleryImageName, galleryImageVersionName)
            .map(this::wrapModel);
    }

    @Override
    public GalleryImageVersion getByGalleryImage(
        String resourceGroupName, String galleryName, String galleryImageName, String galleryImageVersionName) {
        return this
            .getByGalleryImageAsync(resourceGroupName, galleryName, galleryImageName, galleryImageVersionName)
            .block();
    }

    @Override
    public Mono<Void> deleteByGalleryImageAsync(
        String resourceGroupName, String galleryName, String galleryImageName, String galleryImageVersionName) {
        return innerModel().deleteAsync(resourceGroupName, galleryName, galleryImageName, galleryImageVersionName);
    }

    @Override
    public void deleteByGalleryImage(
        String resourceGroupName, String galleryName, String galleryImageName, String galleryImageVersionName) {
        this
            .deleteByGalleryImageAsync(resourceGroupName, galleryName, galleryImageName, galleryImageVersionName)
            .block();
    }
}
