// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.accesshelpers;

import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;

/**
 * Helper class to access non-public APIs of {@link CosmosDatabaseResponse}.
 */
public final class CosmosDatabaseResponseHelper {
    private static CosmosDatabaseResponseAccessor accessor;

    /*
     * Since CosmosDatabaseResponseAccessor has an API that will call the constructor of CosmosDatabaseResponse it will
     * need to ensure that CosmosDatabaseResponse has been loaded.
     */
    static {
        // Access the CosmosDatabaseResponse class to ensure it has been loaded.
        try {
            Class<?> ensureLoaded = Class.forName(CosmosDatabaseResponse.class.getName());
        } catch (ClassNotFoundException ex) {
            // This should never happen.
            throw new RuntimeException(ex);
        }
    }

    /**
     * Type defining the methods that access non-public APIs of {@link CosmosDatabaseResponse}.
     */
    public interface CosmosDatabaseResponseAccessor {
        /**
         * Creates a new instance of {@link CosmosDatabaseResponse}.
         *
         * @param response The {@link ResourceResponse} the database response is based on.
         * @return A new instance of {@link CosmosDatabaseResponse}.
         */
        CosmosDatabaseResponse createCosmosDatabaseResponse(ResourceResponse<Database> response);
    }

    /**
     * The method called from {@link CosmosDatabaseResponse} to set its accessor.
     *
     * @param cosmosDatabaseResponseAccessor The accessor.
     */
    public static void setAccessor(final CosmosDatabaseResponseAccessor cosmosDatabaseResponseAccessor) {
        accessor = cosmosDatabaseResponseAccessor;
    }

    /**
     * Creates a new instance of {@link CosmosDatabaseResponse}.
     *
     * @param response The {@link ResourceResponse} the database response is based on.
     * @return A new instance of {@link CosmosDatabaseResponse}.
     */
    public static CosmosDatabaseResponse createCosmosDatabaseResponse(ResourceResponse<Database> response) {
        return accessor.createCosmosDatabaseResponse(response);
    }
}
