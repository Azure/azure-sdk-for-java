// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.AsciiFoldingTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.AsciiFoldingTokenFilter} and
 * {@link AsciiFoldingTokenFilter}.
 */
public final class AsciiFoldingTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.AsciiFoldingTokenFilter} to
     * {@link AsciiFoldingTokenFilter}.
     */
    public static AsciiFoldingTokenFilter map(com.azure.search.documents.indexes.implementation.models.AsciiFoldingTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new AsciiFoldingTokenFilter(obj.getName()).setPreserveOriginal(obj.isPreserveOriginal());
    }

    /**
     * Maps from {@link AsciiFoldingTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.AsciiFoldingTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.AsciiFoldingTokenFilter map(AsciiFoldingTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.AsciiFoldingTokenFilter(obj.getName())
            .setPreserveOriginal(obj.isPreserveOriginal());
    }

    private AsciiFoldingTokenFilterConverter() {
    }
}
