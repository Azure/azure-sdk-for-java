// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.MicrosoftLanguageStemmingTokenizer;
import com.azure.search.documents.models.MicrosoftStemmingTokenizerLanguage;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.MicrosoftLanguageStemmingTokenizer}
 * and {@link MicrosoftLanguageStemmingTokenizer}.
 */
public final class MicrosoftLanguageStemmingTokenizerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(MicrosoftLanguageStemmingTokenizerConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.MicrosoftLanguageStemmingTokenizer} to
     * {@link MicrosoftLanguageStemmingTokenizer}.
     */
    public static MicrosoftLanguageStemmingTokenizer map(com.azure.search.documents.implementation.models.MicrosoftLanguageStemmingTokenizer obj) {
        if (obj == null) {
            return null;
        }
        MicrosoftLanguageStemmingTokenizer microsoftLanguageStemmingTokenizer =
            new MicrosoftLanguageStemmingTokenizer();

        String _name = obj.getName();
        microsoftLanguageStemmingTokenizer.setName(_name);

        Integer _maxTokenLength = obj.getMaxTokenLength();
        microsoftLanguageStemmingTokenizer.setMaxTokenLength(_maxTokenLength);

        if (obj.getLanguage() != null) {
            MicrosoftStemmingTokenizerLanguage _language =
                MicrosoftStemmingTokenizerLanguageConverter.map(obj.getLanguage());
            microsoftLanguageStemmingTokenizer.setLanguage(_language);
        }

        Boolean _isSearchTokenizer = obj.isSearchTokenizer();
        microsoftLanguageStemmingTokenizer.setIsSearchTokenizer(_isSearchTokenizer);
        return microsoftLanguageStemmingTokenizer;
    }

    /**
     * Maps from {@link MicrosoftLanguageStemmingTokenizer} to
     * {@link com.azure.search.documents.implementation.models.MicrosoftLanguageStemmingTokenizer}.
     */
    public static com.azure.search.documents.implementation.models.MicrosoftLanguageStemmingTokenizer map(MicrosoftLanguageStemmingTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.MicrosoftLanguageStemmingTokenizer microsoftLanguageStemmingTokenizer = new com.azure.search.documents.implementation.models.MicrosoftLanguageStemmingTokenizer();

        String _name = obj.getName();
        microsoftLanguageStemmingTokenizer.setName(_name);

        Integer _maxTokenLength = obj.getMaxTokenLength();
        microsoftLanguageStemmingTokenizer.setMaxTokenLength(_maxTokenLength);

        if (obj.getLanguage() != null) {
            com.azure.search.documents.implementation.models.MicrosoftStemmingTokenizerLanguage _language =
                MicrosoftStemmingTokenizerLanguageConverter.map(obj.getLanguage());
            microsoftLanguageStemmingTokenizer.setLanguage(_language);
        }

        Boolean _isSearchTokenizer = obj.isSearchTokenizer();
        microsoftLanguageStemmingTokenizer.setIsSearchTokenizer(_isSearchTokenizer);
        return microsoftLanguageStemmingTokenizer;
    }
}
