// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.collection;

import reactor.core.publisher.Mono;

/**
 * Provides access to update a private endpoint connection.
 */
public interface SupportsUpdatingPrivateEndpointConnection {

    /**
     * Approves the private endpoint connection.
     *
     * @param privateEndpointConnectionName the name of the private endpoint connection.
     */
    void approvePrivateEndpointConnection(String privateEndpointConnectionName);

    /**
     * Approves the private endpoint connection.
     *
     * @param privateEndpointConnectionName the name of the private endpoint connection.
     * @return the completion.
     */
    Mono<Void> approvePrivateEndpointConnectionAsync(String privateEndpointConnectionName);

    /**
     * Rejects the private endpoint connection.
     *
     * @param privateEndpointConnectionName the name of the private endpoint connection.
     */
    void rejectPrivateEndpointConnection(String privateEndpointConnectionName);

    /**
     * Rejects the private endpoint connection.
     *
     * @param privateEndpointConnectionName the name of the private endpoint connection.
     * @return the completion.
     */
    Mono<Void> rejectPrivateEndpointConnectionAsync(String privateEndpointConnectionName);
}
