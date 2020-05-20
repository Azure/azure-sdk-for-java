// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.TokenFilterName;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.TokenFilterName} and
 * {@link TokenFilterName}.
 */
public final class TokenFilterNameConverter {
    private static final ClientLogger LOGGER = new ClientLogger(TokenFilterNameConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.implementation.models.TokenFilterName} to enum
     * {@link TokenFilterName}.
     */
    public static TokenFilterName map(com.azure.search.documents.implementation.models.TokenFilterName obj) {
        if (obj == null) {
            return null;
        }
        return TokenFilterName.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link TokenFilterName} to enum
     * {@link com.azure.search.documents.implementation.models.TokenFilterName}.
     */
    public static com.azure.search.documents.implementation.models.TokenFilterName map(TokenFilterName obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.implementation.models.TokenFilterName.fromString(obj.toString());
    }
}
