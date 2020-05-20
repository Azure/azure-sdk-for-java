// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.SearchIndexerLimits;

import java.time.Duration;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchIndexerLimits} and
 * {@link SearchIndexerLimits}.
 */
public final class SearchIndexerLimitsConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexerLimitsConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SearchIndexerLimits} to
     * {@link SearchIndexerLimits}.
     */
    public static SearchIndexerLimits map(com.azure.search.documents.implementation.models.SearchIndexerLimits obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexerLimits searchIndexerLimits = new SearchIndexerLimits();

        Duration _maxRunTime = obj.getMaxRunTime();
        PrivateFieldAccessHelper.set(searchIndexerLimits, "maxRunTime", _maxRunTime);

        Double _maxDocumentContentCharactersToExtract = obj.getMaxDocumentContentCharactersToExtract();
        PrivateFieldAccessHelper.set(searchIndexerLimits, "maxDocumentContentCharactersToExtract",
            _maxDocumentContentCharactersToExtract);

        Double _maxDocumentExtractionSize = obj.getMaxDocumentExtractionSize();
        PrivateFieldAccessHelper.set(searchIndexerLimits, "maxDocumentExtractionSize", _maxDocumentExtractionSize);
        return searchIndexerLimits;
    }

    /**
     * Maps from {@link SearchIndexerLimits} to
     * {@link com.azure.search.documents.implementation.models.SearchIndexerLimits}.
     */
    public static com.azure.search.documents.implementation.models.SearchIndexerLimits map(SearchIndexerLimits obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SearchIndexerLimits searchIndexerLimits =
            new com.azure.search.documents.implementation.models.SearchIndexerLimits();

        Duration _maxRunTime = obj.getMaxRunTime();
        PrivateFieldAccessHelper.set(searchIndexerLimits, "maxRunTime", _maxRunTime);

        Double _maxDocumentContentCharactersToExtract = obj.getMaxDocumentContentCharactersToExtract();
        PrivateFieldAccessHelper.set(searchIndexerLimits, "maxDocumentContentCharactersToExtract",
            _maxDocumentContentCharactersToExtract);

        Double _maxDocumentExtractionSize = obj.getMaxDocumentExtractionSize();
        PrivateFieldAccessHelper.set(searchIndexerLimits, "maxDocumentExtractionSize", _maxDocumentExtractionSize);
        return searchIndexerLimits;
    }
}
