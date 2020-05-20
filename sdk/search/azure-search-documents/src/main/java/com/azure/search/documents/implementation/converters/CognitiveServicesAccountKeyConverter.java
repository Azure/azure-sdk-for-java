// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.CognitiveServicesAccountKey;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.CognitiveServicesAccountKey} and
 * {@link CognitiveServicesAccountKey}.
 */
public final class CognitiveServicesAccountKeyConverter {
    private static final ClientLogger LOGGER = new ClientLogger(CognitiveServicesAccountKeyConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.CognitiveServicesAccountKey} to
     * {@link CognitiveServicesAccountKey}.
     */
    public static CognitiveServicesAccountKey map(com.azure.search.documents.implementation.models.CognitiveServicesAccountKey obj) {
        if (obj == null) {
            return null;
        }
        CognitiveServicesAccountKey cognitiveServicesAccountKey = new CognitiveServicesAccountKey();

        String _description = obj.getDescription();
        cognitiveServicesAccountKey.setDescription(_description);

        String _key = obj.getKey();
        cognitiveServicesAccountKey.setKey(_key);
        return cognitiveServicesAccountKey;
    }

    /**
     * Maps from {@link CognitiveServicesAccountKey} to
     * {@link com.azure.search.documents.implementation.models.CognitiveServicesAccountKey}.
     */
    public static com.azure.search.documents.implementation.models.CognitiveServicesAccountKey map(CognitiveServicesAccountKey obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.CognitiveServicesAccountKey cognitiveServicesAccountKey =
            new com.azure.search.documents.implementation.models.CognitiveServicesAccountKey();

        String _description = obj.getDescription();
        cognitiveServicesAccountKey.setDescription(_description);

        String _key = obj.getKey();
        cognitiveServicesAccountKey.setKey(_key);
        return cognitiveServicesAccountKey;
    }
}
