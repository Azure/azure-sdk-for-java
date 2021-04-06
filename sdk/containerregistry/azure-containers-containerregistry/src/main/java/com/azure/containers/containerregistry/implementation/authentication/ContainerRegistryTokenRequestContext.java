// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

/**
 * A token request context associated with a given container registry token.
 */
class ContainerRegistryTokenRequestContext {
    private final String scope;
    private final String serviceName;

    /**
     * Creates an instance of TokenRequestContext.
     * @param serviceName the service name of the registry.
     * @param scope token scope.
     */
    ContainerRegistryTokenRequestContext(String serviceName, String scope) {
        this.serviceName = serviceName;
        this.scope = scope;
    }

    /**
     * Get the service name.
     * @return service name.
     */
    String getServiceName() {
        return this.serviceName;
    }

    /**
     * Get's the token scope.
     * @return scope for the context.
     */
    String getScope() {
        return this.scope;
    }
}
