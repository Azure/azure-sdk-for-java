// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.PhoneticEncoder;
import com.azure.search.documents.models.PhoneticTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.PhoneticTokenFilter} and
 * {@link PhoneticTokenFilter}.
 */
public final class PhoneticTokenFilterConverter {


    /**
     * Maps from {@link com.azure.search.documents.implementation.models.PhoneticTokenFilter} to
     * {@link PhoneticTokenFilter}.
     */
    public static PhoneticTokenFilter map(com.azure.search.documents.implementation.models.PhoneticTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        PhoneticTokenFilter phoneticTokenFilter = new PhoneticTokenFilter();

        String name = obj.getName();
        phoneticTokenFilter.setName(name);

        Boolean replaceOriginalTokens = obj.isReplaceOriginalTokens();
        phoneticTokenFilter.setReplaceOriginalTokens(replaceOriginalTokens);

        if (obj.getEncoder() != null) {
            PhoneticEncoder encoder = PhoneticEncoderConverter.map(obj.getEncoder());
            phoneticTokenFilter.setEncoder(encoder);
        }
        return phoneticTokenFilter;
    }

    /**
     * Maps from {@link PhoneticTokenFilter} to
     * {@link com.azure.search.documents.implementation.models.PhoneticTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.PhoneticTokenFilter map(PhoneticTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.PhoneticTokenFilter phoneticTokenFilter =
            new com.azure.search.documents.implementation.models.PhoneticTokenFilter();

        String name = obj.getName();
        phoneticTokenFilter.setName(name);

        Boolean replaceOriginalTokens = obj.isReplaceOriginalTokens();
        phoneticTokenFilter.setReplaceOriginalTokens(replaceOriginalTokens);

        if (obj.getEncoder() != null) {
            com.azure.search.documents.implementation.models.PhoneticEncoder encoder =
                PhoneticEncoderConverter.map(obj.getEncoder());
            phoneticTokenFilter.setEncoder(encoder);
        }
        return phoneticTokenFilter;
    }

    private PhoneticTokenFilterConverter() {
    }
}
