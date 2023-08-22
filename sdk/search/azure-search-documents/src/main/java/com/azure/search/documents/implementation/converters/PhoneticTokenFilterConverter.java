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
        PhoneticTokenFilter phoneticTokenFilter = new PhoneticTokenFilter(obj.getName());

        Boolean replaceOriginalTokens = obj.isReplaceOriginalTokens();
        phoneticTokenFilter.setOriginalTokensReplaced(replaceOriginalTokens);

        if (obj.getEncoder() != null) {
            phoneticTokenFilter.setEncoder(obj.getEncoder());
        }
        return phoneticTokenFilter;
    }

    /**
     * Maps from {@link PhoneticTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.PhoneticTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.PhoneticTokenFilter map(PhoneticTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.PhoneticTokenFilter phoneticTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.PhoneticTokenFilter(obj.getName());

        Boolean replaceOriginalTokens = obj.areOriginalTokensReplaced();
        phoneticTokenFilter.setReplaceOriginalTokens(replaceOriginalTokens);

        if (obj.getEncoder() != null) {
            phoneticTokenFilter.setEncoder(obj.getEncoder());
        }

        return phoneticTokenFilter;
    }

    private PhoneticTokenFilterConverter() {
    }
}
