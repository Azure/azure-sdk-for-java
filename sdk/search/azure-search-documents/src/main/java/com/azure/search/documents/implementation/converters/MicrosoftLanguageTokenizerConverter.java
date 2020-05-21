// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.MicrosoftLanguageTokenizer;
import com.azure.search.documents.models.MicrosoftTokenizerLanguage;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.MicrosoftLanguageTokenizer} and
 * {@link MicrosoftLanguageTokenizer}.
 */
public final class MicrosoftLanguageTokenizerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(MicrosoftLanguageTokenizerConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.MicrosoftLanguageTokenizer} to
     * {@link MicrosoftLanguageTokenizer}.
     */
    public static MicrosoftLanguageTokenizer map(com.azure.search.documents.implementation.models.MicrosoftLanguageTokenizer obj) {
        if (obj == null) {
            return null;
        }
        MicrosoftLanguageTokenizer microsoftLanguageTokenizer = new MicrosoftLanguageTokenizer();

        String _name = obj.getName();
        microsoftLanguageTokenizer.setName(_name);

        Integer _maxTokenLength = obj.getMaxTokenLength();
        microsoftLanguageTokenizer.setMaxTokenLength(_maxTokenLength);

        if (obj.getLanguage() != null) {
            MicrosoftTokenizerLanguage _language = MicrosoftTokenizerLanguageConverter.map(obj.getLanguage());
            microsoftLanguageTokenizer.setLanguage(_language);
        }

        Boolean _isSearchTokenizer = obj.isSearchTokenizer();
        microsoftLanguageTokenizer.setIsSearchTokenizer(_isSearchTokenizer);
        return microsoftLanguageTokenizer;
    }

    /**
     * Maps from {@link MicrosoftLanguageTokenizer} to
     * {@link com.azure.search.documents.implementation.models.MicrosoftLanguageTokenizer}.
     */
    public static com.azure.search.documents.implementation.models.MicrosoftLanguageTokenizer map(MicrosoftLanguageTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.MicrosoftLanguageTokenizer microsoftLanguageTokenizer =
            new com.azure.search.documents.implementation.models.MicrosoftLanguageTokenizer();

        String _name = obj.getName();
        microsoftLanguageTokenizer.setName(_name);

        Integer _maxTokenLength = obj.getMaxTokenLength();
        microsoftLanguageTokenizer.setMaxTokenLength(_maxTokenLength);

        if (obj.getLanguage() != null) {
            com.azure.search.documents.implementation.models.MicrosoftTokenizerLanguage _language =
                MicrosoftTokenizerLanguageConverter.map(obj.getLanguage());
            microsoftLanguageTokenizer.setLanguage(_language);
        }

        Boolean _isSearchTokenizer = obj.isSearchTokenizer();
        microsoftLanguageTokenizer.setIsSearchTokenizer(_isSearchTokenizer);
        return microsoftLanguageTokenizer;
    }
}
