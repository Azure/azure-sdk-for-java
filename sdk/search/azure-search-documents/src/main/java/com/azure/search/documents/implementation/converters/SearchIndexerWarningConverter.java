// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.indexes.models.SearchIndexerWarning;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerWarning} and
 * {@link SearchIndexerWarning}.
 */
public final class SearchIndexerWarningConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerWarning} to
     * {@link SearchIndexerWarning}.
     */
    public static SearchIndexerWarning map(com.azure.search.documents.indexes.implementation.models.SearchIndexerWarning obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexerWarning searchIndexerWarning = new SearchIndexerWarning();

        String name = obj.getName();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "name", name);

        String details = obj.getDetails();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "details", details);

        String documentationLink = obj.getDocumentationLink();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "documentationLink", documentationLink);

        String message = obj.getMessage();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "message", message);

        String key = obj.getKey();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "key", key);
        return searchIndexerWarning;
    }

    /**
     * Maps from {@link SearchIndexerWarning} to
     * {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerWarning}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SearchIndexerWarning map(SearchIndexerWarning obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.SearchIndexerWarning searchIndexerWarning =
            new com.azure.search.documents.indexes.implementation.models.SearchIndexerWarning();

        String name = obj.getName();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "name", name);

        String details = obj.getDetails();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "details", details);

        String documentationLink = obj.getDocumentationLink();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "documentationLink", documentationLink);

        String message = obj.getMessage();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "message", message);

        String key = obj.getKey();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "key", key);
        return searchIndexerWarning;
    }

    private SearchIndexerWarningConverter() {
    }
}
