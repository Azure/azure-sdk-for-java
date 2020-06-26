// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.CorsOptions;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.CorsOptions} and {@link CorsOptions}.
 */
public final class CorsOptionsConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.CorsOptions} to {@link CorsOptions}.
     */
    public static CorsOptions map(com.azure.search.documents.indexes.implementation.models.CorsOptions obj) {
        if (obj == null) {
            return null;
        }
        CorsOptions corsOptions = new CorsOptions(obj.getAllowedOrigins());

        Long maxAgeInSeconds = obj.getMaxAgeInSeconds();
        corsOptions.setMaxAgeInSeconds(maxAgeInSeconds);
        return corsOptions;
    }

    /**
     * Maps from {@link CorsOptions} to {@link com.azure.search.documents.indexes.implementation.models.CorsOptions}.
     */
    public static com.azure.search.documents.indexes.implementation.models.CorsOptions map(CorsOptions obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.CorsOptions corsOptions =
            new com.azure.search.documents.indexes.implementation.models.CorsOptions(obj.getAllowedOrigins());

        Long maxAgeInSeconds = obj.getMaxAgeInSeconds();
        corsOptions.setMaxAgeInSeconds(maxAgeInSeconds);
        corsOptions.validate();
        return corsOptions;
    }

    private CorsOptionsConverter() {
    }
}
