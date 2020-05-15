package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.CognitiveServicesAccount;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.CognitiveServicesAccount} and
 * {@link CognitiveServicesAccount} mismatch.
 */
public final class CognitiveServicesAccountConverter {
    public static CognitiveServicesAccount convert(com.azure.search.documents.models.CognitiveServicesAccount obj) {
        return DefaultConverter.convert(obj, CognitiveServicesAccount.class);
    }

    public static com.azure.search.documents.models.CognitiveServicesAccount convert(CognitiveServicesAccount obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.CognitiveServicesAccount.class);
    }
}
