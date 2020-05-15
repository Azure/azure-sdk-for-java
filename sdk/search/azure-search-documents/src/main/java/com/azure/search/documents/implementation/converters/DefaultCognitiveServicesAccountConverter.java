package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.DefaultCognitiveServicesAccount;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.DefaultCognitiveServicesAccount} and
 * {@link DefaultCognitiveServicesAccount} mismatch.
 */
public final class DefaultCognitiveServicesAccountConverter {
    public static DefaultCognitiveServicesAccount convert(com.azure.search.documents.models.DefaultCognitiveServicesAccount obj) {
        return DefaultConverter.convert(obj, DefaultCognitiveServicesAccount.class);
    }

    public static com.azure.search.documents.models.DefaultCognitiveServicesAccount convert(DefaultCognitiveServicesAccount obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.DefaultCognitiveServicesAccount.class);
    }
}
