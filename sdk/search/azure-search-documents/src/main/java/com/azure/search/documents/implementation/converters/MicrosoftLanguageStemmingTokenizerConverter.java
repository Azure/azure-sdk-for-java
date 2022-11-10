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
        MicrosoftLanguageStemmingTokenizer microsoftLanguageStemmingTokenizer =
            new MicrosoftLanguageStemmingTokenizer(obj.getName());

        Integer maxTokenLength = obj.getMaxTokenLength();
        microsoftLanguageStemmingTokenizer.setMaxTokenLength(maxTokenLength);

        if (obj.getLanguage() != null) {
            microsoftLanguageStemmingTokenizer.setLanguage(obj.getLanguage());
        }

        Boolean isSearchTokenizer = obj.isSearchTokenizer();
        microsoftLanguageStemmingTokenizer.setIsSearchTokenizerUsed(isSearchTokenizer);
        return microsoftLanguageStemmingTokenizer;
    }

    /**
     * Maps from {@link MicrosoftLanguageStemmingTokenizer} to
     * {@link com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageStemmingTokenizer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageStemmingTokenizer map(MicrosoftLanguageStemmingTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageStemmingTokenizer microsoftLanguageStemmingTokenizer
            = new com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageStemmingTokenizer(obj.getName());

        Integer maxTokenLength = obj.getMaxTokenLength();
        microsoftLanguageStemmingTokenizer.setMaxTokenLength(maxTokenLength);

        if (obj.getLanguage() != null) {
            microsoftLanguageStemmingTokenizer.setLanguage(obj.getLanguage());
        }

        Boolean isSearchTokenizer = obj.isSearchTokenizer();
        microsoftLanguageStemmingTokenizer.setIsSearchTokenizer(isSearchTokenizer);
        return microsoftLanguageStemmingTokenizer;
    }

    private MicrosoftLanguageStemmingTokenizerConverter() {
    }
}
