// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.CjkBigramTokenFilter;
import com.azure.search.documents.models.CjkBigramTokenFilterScripts;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.CjkBigramTokenFilter} and
 * {@link CjkBigramTokenFilter}.
 */
public final class CjkBigramTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(CjkBigramTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.CjkBigramTokenFilter} to
     * {@link CjkBigramTokenFilter}.
     */
    public static CjkBigramTokenFilter map(com.azure.search.documents.implementation.models.CjkBigramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        CjkBigramTokenFilter cjkBigramTokenFilter = new CjkBigramTokenFilter();

        String _name = obj.getName();
        cjkBigramTokenFilter.setName(_name);

        Boolean _outputUnigrams = obj.isOutputUnigrams();
        cjkBigramTokenFilter.setOutputUnigrams(_outputUnigrams);

        if (obj.getIgnoreScripts() != null) {
            List<CjkBigramTokenFilterScripts> _ignoreScripts =
                obj.getIgnoreScripts().stream().map(CjkBigramTokenFilterScriptsConverter::map).collect(Collectors.toList());
            cjkBigramTokenFilter.setIgnoreScripts(_ignoreScripts);
        }
        return cjkBigramTokenFilter;
    }

    /**
     * Maps from {@link CjkBigramTokenFilter} to
     * {@link com.azure.search.documents.implementation.models.CjkBigramTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.CjkBigramTokenFilter map(CjkBigramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.CjkBigramTokenFilter cjkBigramTokenFilter =
            new com.azure.search.documents.implementation.models.CjkBigramTokenFilter();

        String _name = obj.getName();
        cjkBigramTokenFilter.setName(_name);

        Boolean _outputUnigrams = obj.isOutputUnigrams();
        cjkBigramTokenFilter.setOutputUnigrams(_outputUnigrams);

        if (obj.getIgnoreScripts() != null) {
            List<com.azure.search.documents.implementation.models.CjkBigramTokenFilterScripts> _ignoreScripts =
                obj.getIgnoreScripts().stream().map(CjkBigramTokenFilterScriptsConverter::map).collect(Collectors.toList());
            cjkBigramTokenFilter.setIgnoreScripts(_ignoreScripts);
        }
        return cjkBigramTokenFilter;
    }
}
