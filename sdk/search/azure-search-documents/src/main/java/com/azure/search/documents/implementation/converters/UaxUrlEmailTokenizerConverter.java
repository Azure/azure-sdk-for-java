// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.UaxUrlEmailTokenizer;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.UaxUrlEmailTokenizer} and
 * {@link UaxUrlEmailTokenizer}.
 */
public final class UaxUrlEmailTokenizerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(UaxUrlEmailTokenizerConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.UaxUrlEmailTokenizer} to
     * {@link UaxUrlEmailTokenizer}.
     */
    public static UaxUrlEmailTokenizer map(com.azure.search.documents.implementation.models.UaxUrlEmailTokenizer obj) {
        if (obj == null) {
            return null;
        }
        UaxUrlEmailTokenizer uaxUrlEmailTokenizer = new UaxUrlEmailTokenizer();

        String _name = obj.getName();
        uaxUrlEmailTokenizer.setName(_name);

        Integer _maxTokenLength = obj.getMaxTokenLength();
        uaxUrlEmailTokenizer.setMaxTokenLength(_maxTokenLength);
        return uaxUrlEmailTokenizer;
    }

    /**
     * Maps from {@link UaxUrlEmailTokenizer} to
     * {@link com.azure.search.documents.implementation.models.UaxUrlEmailTokenizer}.
     */
    public static com.azure.search.documents.implementation.models.UaxUrlEmailTokenizer map(UaxUrlEmailTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.UaxUrlEmailTokenizer uaxUrlEmailTokenizer =
            new com.azure.search.documents.implementation.models.UaxUrlEmailTokenizer();

        String _name = obj.getName();
        uaxUrlEmailTokenizer.setName(_name);

        Integer _maxTokenLength = obj.getMaxTokenLength();
        uaxUrlEmailTokenizer.setMaxTokenLength(_maxTokenLength);
        return uaxUrlEmailTokenizer;
    }
}
