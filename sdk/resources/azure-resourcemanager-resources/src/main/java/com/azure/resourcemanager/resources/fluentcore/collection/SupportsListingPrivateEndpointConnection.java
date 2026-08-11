// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.collection;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointConnection;

/**
 * Provides access to listing Private endpoint connections to an Azure resource.
 */
public interface SupportsListingPrivateEndpointConnection {

    /**
     * Gets the collection of Private endpoint connection.
     *
     * @return the collection of Private endpoint connection.
     */
    PagedIterable<PrivateEndpointConnection> listPrivateEndpointConnections();

    /**
     * Gets the collection of Private endpoint connection.
     *
     * @return the collection of Private endpoint connection.
     */
    PagedFlux<PrivateEndpointConnection> listPrivateEndpointConnectionsAsync();
}
