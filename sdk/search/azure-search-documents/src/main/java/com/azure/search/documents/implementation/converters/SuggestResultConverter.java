// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.SuggestResult;

import java.util.Map;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SuggestResult} and {@link SuggestResult}.
 */
public final class SuggestResultConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SuggestResult} to {@link SuggestResult}.
     */
    public static SuggestResult map(com.azure.search.documents.implementation.models.SuggestResult obj) {
        if (obj == null) {
            return null;
        }
        SuggestResult suggestResult = new SuggestResult();

        SearchDocument additionalProperties = new SearchDocument(obj.getAdditionalProperties());
        PrivateFieldAccessHelper.set(suggestResult, "additionalProperties", additionalProperties);

        String text = obj.getText();
        PrivateFieldAccessHelper.set(suggestResult, "text", text);
        return suggestResult;
    }

    /**
     * Maps from {@link SuggestResult} to {@link com.azure.search.documents.implementation.models.SuggestResult}.
     */
    public static com.azure.search.documents.implementation.models.SuggestResult map(SuggestResult obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SuggestResult suggestResult =
            new com.azure.search.documents.implementation.models.SuggestResult();

        Map<String, Object> additionalProperties = obj.getDocument();
        PrivateFieldAccessHelper.set(suggestResult, "additionalProperties", additionalProperties);

        String text = obj.getText();
        PrivateFieldAccessHelper.set(suggestResult, "text", text);
        return suggestResult;
    }

    private SuggestResultConverter() {
    }
}
