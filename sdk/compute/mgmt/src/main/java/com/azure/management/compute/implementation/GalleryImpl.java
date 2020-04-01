/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.compute.models.GalleryInner;
import com.azure.management.compute.Gallery;
import com.azure.management.compute.GalleryImage;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation for Gallery and its create and update interfaces.
 */
class GalleryImpl
        extends GroupableResourceImpl<Gallery, GalleryInner, GalleryImpl, ComputeManager>
        implements Gallery, Gallery.Definition, Gallery.Update {
    GalleryImpl(String name, GalleryInner inner, ComputeManager manager) {
        super(name, inner, manager);
    }

    @Override
    public Mono<Gallery> createResourceAsync() {
        return manager().inner().galleries().createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
            .map(innerToFluentMap(this));
    }

    @Override
    public Mono<Gallery> updateResourceAsync() {
        return manager().inner().galleries().createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<GalleryInner> getInnerAsync() {
        return manager().inner().galleries().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().getId() == null;
    }

    @Override
    public String description() {
        return this.inner().description();
    }

    @Override
    public String uniqueName() {
        return this.inner().identifier().uniqueName();
    }

    @Override
    public String provisioningState() {
        return this.inner().provisioningState().toString();
    }

    @Override
    public Mono<GalleryImage> getImageAsync(String imageName) {
        return this.manager().galleryImages().getByGalleryAsync(this.resourceGroupName(), this.name(), imageName);
    }

    @Override
    public GalleryImage getImage(String imageName) {
        return this.manager().galleryImages().getByGallery(this.resourceGroupName(), this.name(), imageName);
    }

    @Override
    public PagedFlux<GalleryImage> listImagesAsync() {
        return this.manager().galleryImages().listByGalleryAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public PagedIterable<GalleryImage> listImages() {
        return this.manager().galleryImages().listByGallery(this.resourceGroupName(), this.name());
    }

    @Override
    public GalleryImpl withDescription(String description) {
        this.inner().withDescription(description);
        return this;
    }
}
