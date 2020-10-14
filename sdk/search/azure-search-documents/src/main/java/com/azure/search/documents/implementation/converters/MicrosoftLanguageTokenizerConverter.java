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
        MicrosoftLanguageTokenizer microsoftLanguageTokenizer = new MicrosoftLanguageTokenizer(obj.getName());

        Integer maxTokenLength = obj.getMaxTokenLength();
        microsoftLanguageTokenizer.setMaxTokenLength(maxTokenLength);

        if (obj.getLanguage() != null) {
            microsoftLanguageTokenizer.setLanguage(obj.getLanguage());
        }

        Boolean isSearchTokenizer = obj.isSearchTokenizer();
        microsoftLanguageTokenizer.setIsSearchTokenizer(isSearchTokenizer);
        return microsoftLanguageTokenizer;
    }

    /**
     * Maps from {@link MicrosoftLanguageTokenizer} to
     * {@link com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageTokenizer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageTokenizer map(MicrosoftLanguageTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageTokenizer microsoftLanguageTokenizer =
            new com.azure.search.documents.indexes.implementation.models.MicrosoftLanguageTokenizer(obj.getName());

        Integer maxTokenLength = obj.getMaxTokenLength();
        microsoftLanguageTokenizer.setMaxTokenLength(maxTokenLength);

        if (obj.getLanguage() != null) {
            microsoftLanguageTokenizer.setLanguage(obj.getLanguage());
        }

        Boolean isSearchTokenizer = obj.isSearchTokenizer();
        microsoftLanguageTokenizer.setIsSearchTokenizer(isSearchTokenizer);
        return microsoftLanguageTokenizer;
    }

    private MicrosoftLanguageTokenizerConverter() {
    }
}
