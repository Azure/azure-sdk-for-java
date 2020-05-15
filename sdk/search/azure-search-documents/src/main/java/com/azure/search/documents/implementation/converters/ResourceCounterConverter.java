package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ResourceCounter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ResourceCounter} and
 * {@link ResourceCounter} mismatch.
 */
public final class ResourceCounterConverter {
    public static ResourceCounter convert(com.azure.search.documents.models.ResourceCounter obj) {
        return DefaultConverter.convert(obj, ResourceCounter.class);
    }

    public static com.azure.search.documents.models.ResourceCounter convert(ResourceCounter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ResourceCounter.class);
    }
}
