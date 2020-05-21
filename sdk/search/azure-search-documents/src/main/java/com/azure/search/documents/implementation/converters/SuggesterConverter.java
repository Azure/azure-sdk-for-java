// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.Suggester;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.Suggester} and {@link Suggester}.
 */
public final class SuggesterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SuggesterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.Suggester} to {@link Suggester}.
     */
    public static Suggester map(com.azure.search.documents.implementation.models.Suggester obj) {
        if (obj == null) {
            return null;
        }
        Suggester suggester = new Suggester();

        if (obj.getSourceFields() != null) {
            List<String> _sourceFields = new ArrayList<>(obj.getSourceFields());
            suggester.setSourceFields(_sourceFields);
        }

        String _name = obj.getName();
        suggester.setName(_name);

        String _searchMode = obj.getSearchMode();
        PrivateFieldAccessHelper.set(suggester, "searchMode", _searchMode);
        return suggester;
    }

    /**
     * Maps from {@link Suggester} to {@link com.azure.search.documents.implementation.models.Suggester}.
     */
    public static com.azure.search.documents.implementation.models.Suggester map(Suggester obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.Suggester suggester =
            new com.azure.search.documents.implementation.models.Suggester();

        if (obj.getSourceFields() != null) {
            List<String> _sourceFields = new ArrayList<>(obj.getSourceFields());
            suggester.setSourceFields(_sourceFields);
        }

        String _name = obj.getName();
        suggester.setName(_name);

        suggester.setSearchMode("analyzingInfixMatching");
        return suggester;
    }
}
