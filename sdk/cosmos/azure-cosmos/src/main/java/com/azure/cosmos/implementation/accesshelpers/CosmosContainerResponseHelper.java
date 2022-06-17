// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.accesshelpers;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.models.CosmosContainerResponse;

/**
 * Helper class to access non-public APIs of {@link CosmosContainerResponse}.
 */
public final class CosmosContainerResponseHelper {
    private static CosmosContainerResponseAccessor accessor;

    /*
     * Since CosmosContainerResponseHelper has an API that will call the constructor of CosmosContainerResponse it will
     * need to ensure that CosmosContainerResponse has been loaded.
     */
    static {
        // Access the CosmosContainerResponse class to ensure it has been loaded.
        try {
            Class<?> ensureLoaded = Class.forName(CosmosContainerResponse.class.getName());
        } catch (ClassNotFoundException ex) {
            // This should never happen.
            throw new RuntimeException(ex);
        }
    }

    /**
     * Type defining the methods that access non-public APIs of {@link CosmosContainerResponse}.
     */
    public interface CosmosContainerResponseAccessor {
        /**
         * Creates a new instance of {@link CosmosContainerResponse}.
         *
         * @param response The {@link ResourceResponse} the container response is based on.
         * @return A new instance of {@link CosmosContainerResponse}.
         */
        CosmosContainerResponse createCosmosContainerResponse(ResourceResponse<DocumentCollection> response);
    }

    /**
     * The method called from {@link CosmosContainerResponse} to set its accessor.
     *
     * @param cosmosContainerResponseAccessor The accessor.
     */
    public static void setAccessor(final CosmosContainerResponseAccessor cosmosContainerResponseAccessor) {
        accessor = cosmosContainerResponseAccessor;
    }

    /**
     * Creates a new instance of {@link CosmosContainerResponse}.
     *
     * @param response The {@link ResourceResponse} the container response is based on.
     * @return A new instance of {@link CosmosContainerResponse}.
     */
    public static CosmosContainerResponse createCosmosContainerResponse(ResourceResponse<DocumentCollection> response) {
        return accessor.createCosmosContainerResponse(response);
    }
}
