// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.indexes.models.SearchIndexerError;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerError} and
 * {@link SearchIndexerError}.
 */
public final class SearchIndexerErrorConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerError} to
     * {@link SearchIndexerError}.
     */
    public static SearchIndexerError map(com.azure.search.documents.indexes.implementation.models.SearchIndexerError obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexerError searchIndexerError = new SearchIndexerError();

        String errorMessage = obj.getErrorMessage();
        PrivateFieldAccessHelper.set(searchIndexerError, "errorMessage", errorMessage);

        String name = obj.getName();
        PrivateFieldAccessHelper.set(searchIndexerError, "name", name);

        String details = obj.getDetails();
        PrivateFieldAccessHelper.set(searchIndexerError, "details", details);

        String documentationLink = obj.getDocumentationLink();
        PrivateFieldAccessHelper.set(searchIndexerError, "documentationLink", documentationLink);

        String key = obj.getKey();
        PrivateFieldAccessHelper.set(searchIndexerError, "key", key);

        int statusCode = obj.getStatusCode();
        PrivateFieldAccessHelper.set(searchIndexerError, "statusCode", statusCode);
        return searchIndexerError;
    }

    /**
     * Maps from {@link SearchIndexerError} to
     * {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerError}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SearchIndexerError map(SearchIndexerError obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.SearchIndexerError searchIndexerError =
            new com.azure.search.documents.indexes.implementation.models.SearchIndexerError();

        String errorMessage = obj.getErrorMessage();
        PrivateFieldAccessHelper.set(searchIndexerError, "errorMessage", errorMessage);

        String name = obj.getName();
        PrivateFieldAccessHelper.set(searchIndexerError, "name", name);

        String details = obj.getDetails();
        PrivateFieldAccessHelper.set(searchIndexerError, "details", details);

        String documentationLink = obj.getDocumentationLink();
        PrivateFieldAccessHelper.set(searchIndexerError, "documentationLink", documentationLink);

        String key = obj.getKey();
        PrivateFieldAccessHelper.set(searchIndexerError, "key", key);

        int statusCode = obj.getStatusCode();
        PrivateFieldAccessHelper.set(searchIndexerError, "statusCode", statusCode);
        return searchIndexerError;
    }

    private SearchIndexerErrorConverter() {
    }
}
