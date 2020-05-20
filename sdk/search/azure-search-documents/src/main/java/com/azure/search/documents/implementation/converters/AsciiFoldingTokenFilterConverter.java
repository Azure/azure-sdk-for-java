// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.AsciiFoldingTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.AsciiFoldingTokenFilter} and
 * {@link AsciiFoldingTokenFilter}.
 */
public final class AsciiFoldingTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(AsciiFoldingTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.AsciiFoldingTokenFilter} to
     * {@link AsciiFoldingTokenFilter}.
     */
    public static AsciiFoldingTokenFilter map(com.azure.search.documents.implementation.models.AsciiFoldingTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        AsciiFoldingTokenFilter asciiFoldingTokenFilter = new AsciiFoldingTokenFilter();

        String _name = obj.getName();
        asciiFoldingTokenFilter.setName(_name);

        Boolean _preserveOriginal = obj.isPreserveOriginal();
        asciiFoldingTokenFilter.setPreserveOriginal(_preserveOriginal);
        return asciiFoldingTokenFilter;
    }

    /**
     * Maps from {@link AsciiFoldingTokenFilter} to
     * {@link com.azure.search.documents.implementation.models.AsciiFoldingTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.AsciiFoldingTokenFilter map(AsciiFoldingTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.AsciiFoldingTokenFilter asciiFoldingTokenFilter =
            new com.azure.search.documents.implementation.models.AsciiFoldingTokenFilter();

        String _name = obj.getName();
        asciiFoldingTokenFilter.setName(_name);

        Boolean _preserveOriginal = obj.isPreserveOriginal();
        asciiFoldingTokenFilter.setPreserveOriginal(_preserveOriginal);
        return asciiFoldingTokenFilter;
    }
}
