package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ServiceLimits;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ServiceLimits} and
 * {@link ServiceLimits} mismatch.
 */
public final class ServiceLimitsConverter {
    public static ServiceLimits convert(com.azure.search.documents.models.ServiceLimits obj) {
        return DefaultConverter.convert(obj, ServiceLimits.class);
    }

    public static com.azure.search.documents.models.ServiceLimits convert(ServiceLimits obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ServiceLimits.class);
    }
}
