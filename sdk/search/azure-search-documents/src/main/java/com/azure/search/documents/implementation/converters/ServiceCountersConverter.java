package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ServiceCounters;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ServiceCounters} and
 * {@link ServiceCounters} mismatch.
 */
public final class ServiceCountersConverter {
    public static ServiceCounters convert(com.azure.search.documents.models.ServiceCounters obj) {
        return DefaultConverter.convert(obj, ServiceCounters.class);
    }

    public static com.azure.search.documents.models.ServiceCounters convert(ServiceCounters obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ServiceCounters.class);
    }
}
