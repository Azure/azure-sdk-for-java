// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.SearchIndexerError;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchIndexerError} and
 * {@link SearchIndexerError}.
 */
public final class SearchIndexerErrorConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexerErrorConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SearchIndexerError} to
     * {@link SearchIndexerError}.
     */
    public static SearchIndexerError map(com.azure.search.documents.implementation.models.SearchIndexerError obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexerError searchIndexerError = new SearchIndexerError();

        String _errorMessage = obj.getErrorMessage();
        PrivateFieldAccessHelper.set(searchIndexerError, "errorMessage", _errorMessage);

        String _name = obj.getName();
        PrivateFieldAccessHelper.set(searchIndexerError, "name", _name);

        String _details = obj.getDetails();
        PrivateFieldAccessHelper.set(searchIndexerError, "details", _details);

        String _documentationLink = obj.getDocumentationLink();
        PrivateFieldAccessHelper.set(searchIndexerError, "documentationLink", _documentationLink);

        String _key = obj.getKey();
        PrivateFieldAccessHelper.set(searchIndexerError, "key", _key);

        int _statusCode = obj.getStatusCode();
        PrivateFieldAccessHelper.set(searchIndexerError, "statusCode", _statusCode);
        return searchIndexerError;
    }

    /**
     * Maps from {@link SearchIndexerError} to
     * {@link com.azure.search.documents.implementation.models.SearchIndexerError}.
     */
    public static com.azure.search.documents.implementation.models.SearchIndexerError map(SearchIndexerError obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SearchIndexerError searchIndexerError =
            new com.azure.search.documents.implementation.models.SearchIndexerError();

        String _errorMessage = obj.getErrorMessage();
        PrivateFieldAccessHelper.set(searchIndexerError, "errorMessage", _errorMessage);

        String _name = obj.getName();
        PrivateFieldAccessHelper.set(searchIndexerError, "name", _name);

        String _details = obj.getDetails();
        PrivateFieldAccessHelper.set(searchIndexerError, "details", _details);

        String _documentationLink = obj.getDocumentationLink();
        PrivateFieldAccessHelper.set(searchIndexerError, "documentationLink", _documentationLink);

        String _key = obj.getKey();
        PrivateFieldAccessHelper.set(searchIndexerError, "key", _key);

        int _statusCode = obj.getStatusCode();
        PrivateFieldAccessHelper.set(searchIndexerError, "statusCode", _statusCode);
        return searchIndexerError;
    }
}
