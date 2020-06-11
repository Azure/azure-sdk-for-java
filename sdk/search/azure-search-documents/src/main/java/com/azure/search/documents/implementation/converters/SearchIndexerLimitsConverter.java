// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.indexes.models.SearchIndexerLimits;

import java.time.Duration;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerLimits} and
 * {@link SearchIndexerLimits}.
 */
public final class SearchIndexerLimitsConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerLimits} to
     * {@link SearchIndexerLimits}.
     */
    public static SearchIndexerLimits map(com.azure.search.documents.indexes.implementation.models.SearchIndexerLimits obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexerLimits searchIndexerLimits = new SearchIndexerLimits();

        Duration maxRunTime = obj.getMaxRunTime();
        PrivateFieldAccessHelper.set(searchIndexerLimits, "maxRunTime", maxRunTime);

        Double maxDocumentContentCharactersToExtract = obj.getMaxDocumentContentCharactersToExtract();
        PrivateFieldAccessHelper.set(searchIndexerLimits, "maxDocumentContentCharactersToExtract",
            maxDocumentContentCharactersToExtract);

        Double maxDocumentExtractionSize = obj.getMaxDocumentExtractionSize();
        PrivateFieldAccessHelper.set(searchIndexerLimits, "maxDocumentExtractionSize", maxDocumentExtractionSize);
        return searchIndexerLimits;
    }

    /**
     * Maps from {@link SearchIndexerLimits} to
     * {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerLimits}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SearchIndexerLimits map(SearchIndexerLimits obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.SearchIndexerLimits searchIndexerLimits =
            new com.azure.search.documents.indexes.implementation.models.SearchIndexerLimits();

        Duration maxRunTime = obj.getMaxRunTime();
        PrivateFieldAccessHelper.set(searchIndexerLimits, "maxRunTime", maxRunTime);

        Double maxDocumentContentCharactersToExtract = obj.getMaxDocumentContentCharactersToExtract();
        PrivateFieldAccessHelper.set(searchIndexerLimits, "maxDocumentContentCharactersToExtract",
            maxDocumentContentCharactersToExtract);

        Double maxDocumentExtractionSize = obj.getMaxDocumentExtractionSize();
        PrivateFieldAccessHelper.set(searchIndexerLimits, "maxDocumentExtractionSize", maxDocumentExtractionSize);
        return searchIndexerLimits;
    }

    private SearchIndexerLimitsConverter() {
    }
}
