// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.UaxUrlEmailTokenizer;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.UaxUrlEmailTokenizer} and
 * {@link UaxUrlEmailTokenizer}.
 */
public final class UaxUrlEmailTokenizerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.UaxUrlEmailTokenizer} to
     * {@link UaxUrlEmailTokenizer}.
     */
    public static UaxUrlEmailTokenizer map(com.azure.search.documents.indexes.implementation.models.UaxUrlEmailTokenizer obj) {
        if (obj == null) {
            return null;
        }
        UaxUrlEmailTokenizer uaxUrlEmailTokenizer = new UaxUrlEmailTokenizer(obj.getName());

        Integer maxTokenLength = obj.getMaxTokenLength();
        uaxUrlEmailTokenizer.setMaxTokenLength(maxTokenLength);
        return uaxUrlEmailTokenizer;
    }

    /**
     * Maps from {@link UaxUrlEmailTokenizer} to
     * {@link com.azure.search.documents.indexes.implementation.models.UaxUrlEmailTokenizer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.UaxUrlEmailTokenizer map(UaxUrlEmailTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.UaxUrlEmailTokenizer uaxUrlEmailTokenizer =
            new com.azure.search.documents.indexes.implementation.models.UaxUrlEmailTokenizer(obj.getName());

        Integer maxTokenLength = obj.getMaxTokenLength();
        uaxUrlEmailTokenizer.setMaxTokenLength(maxTokenLength);

        return uaxUrlEmailTokenizer;
    }

    private UaxUrlEmailTokenizerConverter() {
    }
}
