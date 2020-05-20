// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.CommonGramTokenFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.CommonGramTokenFilter} and
 * {@link CommonGramTokenFilter}.
 */
public final class CommonGramTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(CommonGramTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.CommonGramTokenFilter} to
     * {@link CommonGramTokenFilter}.
     */
    public static CommonGramTokenFilter map(com.azure.search.documents.implementation.models.CommonGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        CommonGramTokenFilter commonGramTokenFilter = new CommonGramTokenFilter();

        String _name = obj.getName();
        commonGramTokenFilter.setName(_name);

        Boolean _ignoreCase = obj.isIgnoreCase();
        commonGramTokenFilter.setIgnoreCase(_ignoreCase);

        Boolean _useQueryMode = obj.isUseQueryMode();
        commonGramTokenFilter.setUseQueryMode(_useQueryMode);

        if (obj.getCommonWords() != null) {
            List<String> _commonWords = new ArrayList<>(obj.getCommonWords());
            commonGramTokenFilter.setCommonWords(_commonWords);
        }
        return commonGramTokenFilter;
    }

    /**
     * Maps from {@link CommonGramTokenFilter} to
     * {@link com.azure.search.documents.implementation.models.CommonGramTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.CommonGramTokenFilter map(CommonGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.CommonGramTokenFilter commonGramTokenFilter =
            new com.azure.search.documents.implementation.models.CommonGramTokenFilter();

        String _name = obj.getName();
        commonGramTokenFilter.setName(_name);

        Boolean _ignoreCase = obj.isIgnoreCase();
        commonGramTokenFilter.setIgnoreCase(_ignoreCase);

        Boolean _useQueryMode = obj.isUseQueryMode();
        commonGramTokenFilter.setUseQueryMode(_useQueryMode);

        if (obj.getCommonWords() != null) {
            List<String> _commonWords = new ArrayList<>(obj.getCommonWords());
            commonGramTokenFilter.setCommonWords(_commonWords);
        }
        return commonGramTokenFilter;
    }
}
