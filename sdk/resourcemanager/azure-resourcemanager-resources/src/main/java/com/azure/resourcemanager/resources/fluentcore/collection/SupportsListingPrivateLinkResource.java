// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.collection;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkResource;

/**
 * Provides access to listing Private link resources for an Azure resource.
 */
public interface SupportsListingPrivateLinkResource {

    /**
     * Gets the collection of supported Private link resource.
     *
     * @return the collection of supported Private link resource.
     */
    PagedIterable<PrivateLinkResource> listPrivateLinkResources();

    /**
     * Gets the collection of supported Private link resource.
     *
     * @return the collection of supported Private link resource.
     */
    PagedFlux<PrivateLinkResource> listPrivateLinkResourcesAsync();
}
