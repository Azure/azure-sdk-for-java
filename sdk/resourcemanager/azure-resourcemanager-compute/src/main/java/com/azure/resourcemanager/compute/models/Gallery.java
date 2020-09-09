// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.inner.GalleryInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure gallery. */
@Fluent
public interface Gallery
    extends HasInner<GalleryInner>,
        Resource,
        GroupableResource<ComputeManager, GalleryInner>,
        HasResourceGroup,
        Refreshable<Gallery>,
        Updatable<Gallery.Update>,
        HasManager<ComputeManager> {
    /** @return description for the gallery resource. */
    String description();

    /** @return the unique name of the gallery resource. */
    String uniqueName();

    /** @return the provisioning state of the gallery resource. */
    String provisioningState();

    /**
     * Retrieves information about an image in the gallery.
     *
     * @param imageName The name of the image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<GalleryImage> getImageAsync(String imageName);

    /**
     * Retrieves information about an image in the gallery.
     *
     * @param imageName The name of the image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the gallery image
     */
    GalleryImage getImage(String imageName);

    /**
     * List images in the gallery.
     *
     * @return the observable for the request
     */
    PagedFlux<GalleryImage> listImagesAsync();

    /**
     * List images in the gallery.
     *
     * @return the list of images in the gallery
     */
    PagedIterable<GalleryImage> listImages();

    /** The entirety of the gallery definition. */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithGroup, DefinitionStages.WithCreate {
    }

    /** Grouping of gallery definition stages. */
    interface DefinitionStages {
        /** The first stage of a gallery definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage of the gallery definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /** The stage of the gallery definition allowing to specify description. */
        interface WithDescription {
            /**
             * Specifies description for the gallery.
             *
             * @param description The description
             * @return the next definition stage
             */
            WithCreate withDescription(String description);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<Gallery>, Resource.DefinitionWithTags<WithCreate>, DefinitionStages.WithDescription {
        }
    }
    /** The template for a Gallery update operation, containing all the settings that can be modified. */
    interface Update extends Appliable<Gallery>, Resource.UpdateWithTags<Update>, UpdateStages.WithDescription {
    }

    /** Grouping of gallery update stages. */
    interface UpdateStages {
        /** The stage of the gallery update allowing to specify description. */
        interface WithDescription {
            /**
             * Specifies description for the gallery.
             *
             * @param description The description
             * @return the next update stage
             */
            Update withDescription(String description);
        }
    }
}
