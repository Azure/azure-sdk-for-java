// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.accesshelpers;

import com.azure.cosmos.implementation.Conflict;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.models.CosmosConflictResponse;

/**
 * Helper class to access non-public APIs of {@link CosmosConflictResponse}
 */
public final class CosmosConflictResponseHelper {
    private static CosmosConflictResponseAccessor accessor;

    /*
     * Since CosmosConflictResponseAccessor has an API that will call the constructor of CosmosConflictResponse it will
     * need to ensure that CosmosConflictResponse has been loaded.
     */
    static {
        // Access the CosmosConflictResponse class to ensure it has been loaded.
        try {
            Class<?> ensureLoaded = Class.forName(CosmosConflictResponse.class.getName());
        } catch (ClassNotFoundException ex) {
            // This should never happen.
            throw new RuntimeException(ex);
        }
    }

    /**
     * Type defining the methods that access non-public APIs of {@link CosmosConflictResponse}.
     */
    public interface CosmosConflictResponseAccessor {
        /**
         * Creates a new instance of {@link CosmosConflictResponse}.
         *
         * @param response The {@link ResourceResponse} the conflict response is based on.
         * @return A new instance of {@link CosmosConflictResponse}.
         */
        CosmosConflictResponse createCosmosConflictResponse(ResourceResponse<Conflict> response);
    }

    /**
     * The method called from {@link CosmosConflictResponse} to set its accessor.
     *
     * @param cosmosConflictResponseAccessor The accessor.
     */
    public static void setAccessor(final CosmosConflictResponseAccessor cosmosConflictResponseAccessor) {
        accessor = cosmosConflictResponseAccessor;
    }

    /**
     * Creates a new instance of {@link CosmosConflictResponse}.
     *
     * @param response The {@link ResourceResponse} the conflict response is based on.
     * @return A new instance of {@link CosmosConflictResponse}.
     */
    public static CosmosConflictResponse createCosmosConflictResponse(ResourceResponse<Conflict> response) {
        return accessor.createCosmosConflictResponse(response);
    }
}
