// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.SearchIndexerWarning;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchIndexerWarning} and
 * {@link SearchIndexerWarning}.
 */
public final class SearchIndexerWarningConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexerWarningConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SearchIndexerWarning} to
     * {@link SearchIndexerWarning}.
     */
    public static SearchIndexerWarning map(com.azure.search.documents.implementation.models.SearchIndexerWarning obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexerWarning searchIndexerWarning = new SearchIndexerWarning();

        String _name = obj.getName();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "name", _name);

        String _details = obj.getDetails();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "details", _details);

        String _documentationLink = obj.getDocumentationLink();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "documentationLink", _documentationLink);

        String _message = obj.getMessage();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "message", _message);

        String _key = obj.getKey();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "key", _key);
        return searchIndexerWarning;
    }

    /**
     * Maps from {@link SearchIndexerWarning} to
     * {@link com.azure.search.documents.implementation.models.SearchIndexerWarning}.
     */
    public static com.azure.search.documents.implementation.models.SearchIndexerWarning map(SearchIndexerWarning obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SearchIndexerWarning searchIndexerWarning =
            new com.azure.search.documents.implementation.models.SearchIndexerWarning();

        String _name = obj.getName();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "name", _name);

        String _details = obj.getDetails();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "details", _details);

        String _documentationLink = obj.getDocumentationLink();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "documentationLink", _documentationLink);

        String _message = obj.getMessage();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "message", _message);

        String _key = obj.getKey();
        PrivateFieldAccessHelper.set(searchIndexerWarning, "key", _key);
        return searchIndexerWarning;
    }
}
