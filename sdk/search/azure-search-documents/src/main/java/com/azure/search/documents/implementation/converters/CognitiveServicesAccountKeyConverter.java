// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.CognitiveServicesAccountKey;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccountKey} and
 * {@link CognitiveServicesAccountKey}.
 */
public final class CognitiveServicesAccountKeyConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccountKey} to
     * {@link CognitiveServicesAccountKey}.
     */
    public static CognitiveServicesAccountKey map(com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccountKey obj) {
        if (obj == null) {
            return null;
        }
        CognitiveServicesAccountKey cognitiveServicesAccountKey = new CognitiveServicesAccountKey(obj.getKey());

        String description = obj.getDescription();
        cognitiveServicesAccountKey.setDescription(description);

        return cognitiveServicesAccountKey;
    }

    /**
     * Maps from {@link CognitiveServicesAccountKey} to
     * {@link com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccountKey}.
     */
    public static com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccountKey map(CognitiveServicesAccountKey obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccountKey cognitiveServicesAccountKey =
            new com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccountKey(obj.getKey());

        String description = obj.getDescription();
        cognitiveServicesAccountKey.setDescription(description);

        return cognitiveServicesAccountKey;
    }

    private CognitiveServicesAccountKeyConverter() {
    }
}
