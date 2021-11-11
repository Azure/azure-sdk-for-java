// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.MicrosoftLanguageStemmingTokenizer;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageStemmingTokenizer}
 * and {@link MicrosoftLanguageStemmingTokenizer}.
 */
public final class MicrosoftLanguageStemmingTokenizerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageStemmingTokenizer} to
     * {@link MicrosoftLanguageStemmingTokenizer}.
     */
    public static MicrosoftLanguageStemmingTokenizer map(com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageStemmingTokenizer obj) {
        if (obj == null) {
            return null;
        }

        return new MicrosoftLanguageStemmingTokenizer(obj.getName())
            .setMaxTokenLength(obj.getMaxTokenLength())
            .setLanguage(obj.getLanguage())
            .setIsSearchTokenizerUsed(obj.isSearchTokenizer());
    }

    /**
     * Maps from {@link MicrosoftLanguageStemmingTokenizer} to {@link com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageStemmingTokenizer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageStemmingTokenizer map(MicrosoftLanguageStemmingTokenizer obj) {
        if (obj == null) {
            return null;
        }
        return new com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageStemmingTokenizer(
            obj.getName())
            .setMaxTokenLength(obj.getMaxTokenLength())
            .setLanguage(obj.getLanguage())
            .setIsSearchTokenizer(obj.isSearchTokenizer());
    }

    private MicrosoftLanguageStemmingTokenizerConverter() {
    }
}
