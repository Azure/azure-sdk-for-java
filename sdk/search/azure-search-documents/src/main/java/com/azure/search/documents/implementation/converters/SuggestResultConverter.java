// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.SuggestResult;

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
        SuggestResult suggestResult = new SuggestResult(obj.getText());

        SearchDocument additionalProperties = new SearchDocument(obj.getAdditionalProperties());
<<<<<<< HEAD
        PrivateFieldAccessHelper.set(suggestResult, "additionalProperties", additionalProperties);

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
            new com.azure.search.documents.implementation.models.SuggestResult(obj.getText());

        SearchDocument additionalProperties = obj.getDocument(SearchDocument.class);
        PrivateFieldAccessHelper.set(suggestResult, "additionalProperties", additionalProperties);
        suggestResult.validate();
=======
        SuggestResultHelper.setAdditionalProperties(suggestResult, additionalProperties);

>>>>>>> bfd056a1647f7232e7d7cb82ca2a5ad85b9bb6ec
        return suggestResult;
    }

    private SuggestResultConverter() {
    }
}
