// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.MicrosoftLanguageStemmingTokenizer;
import com.azure.search.documents.indexes.models.MicrosoftStemmingTokenizerLanguage;

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
            new MicrosoftLanguageStemmingTokenizer();

        String name = obj.getName();
        microsoftLanguageStemmingTokenizer.setName(name);

        Integer maxTokenLength = obj.getMaxTokenLength();
        microsoftLanguageStemmingTokenizer.setMaxTokenLength(maxTokenLength);

        if (obj.getLanguage() != null) {
            MicrosoftStemmingTokenizerLanguage language =
                MicrosoftStemmingTokenizerLanguageConverter.map(obj.getLanguage());
            microsoftLanguageStemmingTokenizer.setLanguage(language);
        }

        Boolean isSearchTokenizer = obj.isSearchTokenizer();
        microsoftLanguageStemmingTokenizer.setIsSearchTokenizer(isSearchTokenizer);
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
        com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageStemmingTokenizer microsoftLanguageStemmingTokenizer = new com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageStemmingTokenizer();

        String name = obj.getName();
        microsoftLanguageStemmingTokenizer.setName(name);

        Integer maxTokenLength = obj.getMaxTokenLength();
        microsoftLanguageStemmingTokenizer.setMaxTokenLength(maxTokenLength);

        if (obj.getLanguage() != null) {
            com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage language =
                MicrosoftStemmingTokenizerLanguageConverter.map(obj.getLanguage());
            microsoftLanguageStemmingTokenizer.setLanguage(language);
        }

        Boolean isSearchTokenizer = obj.isSearchTokenizer();
        microsoftLanguageStemmingTokenizer.setIsSearchTokenizer(isSearchTokenizer);
        return microsoftLanguageStemmingTokenizer;
    }

    private MicrosoftLanguageStemmingTokenizerConverter() {
    }
}
