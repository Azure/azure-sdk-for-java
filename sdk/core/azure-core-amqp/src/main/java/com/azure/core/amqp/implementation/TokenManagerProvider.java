// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.ClaimsBasedSecurityNode;
import reactor.core.publisher.Mono;

/**
 * Provides instances of {@link TokenManager} given the resource type.
 */
public interface TokenManagerProvider {
    /**
     * Returns a token manager given the authorization type, the message broker host, and resource to access.
     *
     * @param cbsNodeMono A {@link Mono} that supplies the CBS node.
     * @param resource The scope/resource to access in the message broker.
     *
     * @return A {@link TokenManager} that manages authorization to the Azure messaging resource.
     */
    TokenManager getTokenManager(Mono<ClaimsBasedSecurityNode> cbsNodeMono, String resource);

    /**
     * Gets the qualified resource scope passed to CBS node for access to the {@code resource}.
     *
     * @param resource The scope/resource to access in the message broker.
     *
     * @return The qualified resource scope to request access from the service.
     */
    String getResourceString(String resource);
}
