// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.PhoneticTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.PhoneticTokenFilter} and
 * {@link PhoneticTokenFilter}.
 */
public final class PhoneticTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.PhoneticTokenFilter} to
     * {@link PhoneticTokenFilter}.
     */
    public static PhoneticTokenFilter map(com.azure.search.documents.indexes.implementation.models.PhoneticTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new PhoneticTokenFilter(obj.getName())
            .setOriginalTokensReplaced(obj.isReplaceOriginalTokens())
            .setEncoder(obj.getEncoder());
    }

    /**
     * Maps from {@link PhoneticTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.PhoneticTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.PhoneticTokenFilter map(PhoneticTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.PhoneticTokenFilter(obj.getName())
            .setReplaceOriginalTokens(obj.areOriginalTokensReplaced())
            .setEncoder(obj.getEncoder());
    }

    private PhoneticTokenFilterConverter() {
    }
}
