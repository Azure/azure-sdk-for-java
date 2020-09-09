// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.fluent.GalleryImageVersionsClient;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/** Entry point to gallery image versions management API in Azure. */
@Fluent
public interface GalleryImageVersions
    extends SupportsCreating<GalleryImageVersion.DefinitionStages.Blank>, HasInner<GalleryImageVersionsClient> {
    /**
     * Retrieves information about a gallery image version.
     *
     * @param resourceGroupName The name of the resource group.
     * @param galleryName The name of the gallery.
     * @param galleryImageName The name of the gallery image.
     * @param galleryImageVersionName The name of the gallery image version.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<GalleryImageVersion> getByGalleryImageAsync(
        String resourceGroupName, String galleryName, String galleryImageName, String galleryImageVersionName);

    /**
     * Retrieves information about a gallery image version.
     *
     * @param resourceGroupName The name of the resource group.
     * @param galleryName The name of the gallery.
     * @param galleryImageName The name of the gallery image.
     * @param galleryImageVersionName The name of the gallery image version.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the gallery image version resource
     */
    GalleryImageVersion getByGalleryImage(
        String resourceGroupName, String galleryName, String galleryImageName, String galleryImageVersionName);

    /**
     * List gallery image versions under a gallery image.
     *
     * @param resourceGroupName The name of the resource group.
     * @param galleryName The name of the gallery.
     * @param galleryImageName The name of the gallery image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    PagedFlux<GalleryImageVersion> listByGalleryImageAsync(
        String resourceGroupName, String galleryName, String galleryImageName);

    /**
     * List gallery image versions under a gallery image.
     *
     * @param resourceGroupName The name of the resource group.
     * @param galleryName The name of the gallery.
     * @param galleryImageName The name of the gallery image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return list of gallery image versions
     */
    PagedIterable<GalleryImageVersion> listByGalleryImage(
        String resourceGroupName, String galleryName, String galleryImageName);

    /**
     * Delete a gallery image version.
     *
     * @param resourceGroupName The name of the resource group.
     * @param galleryName The name of the gallery.
     * @param galleryImageName The name of the gallery image.
     * @param galleryImageVersionName The name of the gallery image version.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the completable for the request
     */
    Mono<Void> deleteByGalleryImageAsync(
        String resourceGroupName, String galleryName, String galleryImageName, String galleryImageVersionName);

    /**
     * Delete a gallery image version.
     *
     * @param resourceGroupName The name of the resource group.
     * @param galleryName The name of the gallery.
     * @param galleryImageName The name of the gallery image.
     * @param galleryImageVersionName The name of the gallery image version.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     */
    void deleteByGalleryImage(
        String resourceGroupName, String galleryName, String galleryImageName, String galleryImageVersionName);
}
