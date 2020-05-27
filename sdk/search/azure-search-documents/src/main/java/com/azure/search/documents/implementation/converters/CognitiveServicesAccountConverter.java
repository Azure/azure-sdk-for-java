// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.CognitiveServicesAccountKey;
import com.azure.search.documents.indexes.models.DefaultCognitiveServicesAccount;
import com.azure.search.documents.indexes.models.CognitiveServicesAccount;

import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ABSTRACT_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccount} and
 * {@link CognitiveServicesAccount}.
 */
public final class CognitiveServicesAccountConverter {
    private static final ClientLogger LOGGER = new ClientLogger(CognitiveServicesAccountConverter.class);

    /**
     * Maps abstract class from {@link com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccount} to
     * {@link CognitiveServicesAccount}. Dedicate works to sub class converter.
     */
    public static CognitiveServicesAccount map(com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccount obj) {
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccountKey) {
            return CognitiveServicesAccountKeyConverter.map((com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccountKey) obj);
        }
        if (obj instanceof com.azure.search.documents.indexes.implementation.models.DefaultCognitiveServicesAccount) {
            return DefaultCognitiveServicesAccountConverter.map((com.azure.search.documents.indexes.implementation.models.DefaultCognitiveServicesAccount) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_EXTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    /**
     * Maps abstract class from {@link CognitiveServicesAccount} to
     * {@link com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccount}. Dedicate works to sub class
     * converter.
     */
    public static com.azure.search.documents.indexes.implementation.models.CognitiveServicesAccount map(CognitiveServicesAccount obj) {
        if (obj instanceof CognitiveServicesAccountKey) {
            return CognitiveServicesAccountKeyConverter.map((CognitiveServicesAccountKey) obj);
        }
        if (obj instanceof DefaultCognitiveServicesAccount) {
            return DefaultCognitiveServicesAccountConverter.map((DefaultCognitiveServicesAccount) obj);
        }
        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ABSTRACT_INTERNAL_ERROR_MSG,
            obj.getClass().getSimpleName())));
    }

    private CognitiveServicesAccountConverter() {
    }
}
