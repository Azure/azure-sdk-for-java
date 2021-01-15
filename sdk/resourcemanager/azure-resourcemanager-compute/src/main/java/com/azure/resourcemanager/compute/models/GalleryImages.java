// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import reactor.core.publisher.Mono;

/** Entry point to gallery images management API in Azure. */
@Fluent
public interface GalleryImages extends SupportsCreating<GalleryImage.DefinitionStages.Blank> {
    /**
     * Retrieves information about an image in a gallery.
     *
     * @param resourceGroupName The name of the resource group.
     * @param galleryName The name of the gallery.
     * @param galleryImageName The name of the gallery image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<GalleryImage> getByGalleryAsync(String resourceGroupName, String galleryName, String galleryImageName);

    /**
     * Retrieves information about an image in a gallery.
     *
     * @param resourceGroupName The name of the resource group.
     * @param galleryName The name of the gallery.
     * @param galleryImageName The name of the gallery image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the gallery image
     */
    GalleryImage getByGallery(String resourceGroupName, String galleryName, String galleryImageName);

    /**
     * List images under a gallery.
     *
     * @param resourceGroupName The name of the resource group.
     * @param galleryName The name of the gallery.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    PagedFlux<GalleryImage> listByGalleryAsync(String resourceGroupName, String galleryName);

    /**
     * List images under a gallery.
     *
     * @param resourceGroupName The name of the resource group.
     * @param galleryName The name of the gallery.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the list of images in the gallery
     */
    PagedIterable<GalleryImage> listByGallery(String resourceGroupName, String galleryName);

    /**
     * Delete a gallery image in a gallery.
     *
     * @param resourceGroupName The name of the resource group.
     * @param galleryName The name of the gallery.
     * @param galleryImageName The name of the gallery image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the completable for the request
     */
    Mono<Void> deleteByGalleryAsync(String resourceGroupName, String galleryName, String galleryImageName);

    /**
     * Delete an image in a gallery.
     *
     * @param resourceGroupName The name of the resource group.
     * @param galleryName The name of the gallery.
     * @param galleryImageName The name of the gallery image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     */
    void deleteByGallery(String resourceGroupName, String galleryName, String galleryImageName);
}
