package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ServiceStatistics;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ServiceStatistics} and
 * {@link ServiceStatistics} mismatch.
 */
public final class ServiceStatisticsConverter {
    public static ServiceStatistics convert(com.azure.search.documents.models.ServiceStatistics obj) {
        return DefaultConverter.convert(obj, ServiceStatistics.class);
    }

    public static com.azure.search.documents.models.ServiceStatistics convert(ServiceStatistics obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ServiceStatistics.class);
    }
}
