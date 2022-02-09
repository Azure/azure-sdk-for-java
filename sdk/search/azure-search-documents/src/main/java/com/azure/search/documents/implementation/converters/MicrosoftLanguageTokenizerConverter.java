// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.MicrosoftLanguageTokenizer;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageTokenizer} and
 * {@link MicrosoftLanguageTokenizer}.
 */
public final class MicrosoftLanguageTokenizerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageTokenizer} to
     * {@link MicrosoftLanguageTokenizer}.
     */
    public static MicrosoftLanguageTokenizer map(com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageTokenizer obj) {
        if (obj == null) {
            return null;
        }

        return new MicrosoftLanguageTokenizer(obj.getName())
            .setMaxTokenLength(obj.getMaxTokenLength())
            .setLanguage(obj.getLanguage())
            .setIsSearchTokenizer(obj.isSearchTokenizer());
    }

    /**
     * Maps from {@link MicrosoftLanguageTokenizer} to
     * {@link com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageTokenizer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageTokenizer map(MicrosoftLanguageTokenizer obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageTokenizer(obj.getName())
            .setMaxTokenLength(obj.getMaxTokenLength())
            .setLanguage(obj.getLanguage())
            .setIsSearchTokenizer(obj.isSearchTokenizer());
    }

    private MicrosoftLanguageTokenizerConverter() {
    }
}
