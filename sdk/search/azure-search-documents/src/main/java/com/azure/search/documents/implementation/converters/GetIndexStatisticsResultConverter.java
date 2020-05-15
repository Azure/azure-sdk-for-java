package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.GetIndexStatisticsResult;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.GetIndexStatisticsResult} and
 * {@link GetIndexStatisticsResult} mismatch.
 */
public final class GetIndexStatisticsResultConverter {
    public static GetIndexStatisticsResult convert(com.azure.search.documents.models.GetIndexStatisticsResult obj) {
        return DefaultConverter.convert(obj, GetIndexStatisticsResult.class);
    }

    public static com.azure.search.documents.models.GetIndexStatisticsResult convert(GetIndexStatisticsResult obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.GetIndexStatisticsResult.class);
    }
}
