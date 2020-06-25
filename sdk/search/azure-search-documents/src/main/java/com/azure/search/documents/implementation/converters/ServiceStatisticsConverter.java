// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ServiceCounters;
import com.azure.search.documents.indexes.models.ServiceLimits;
import com.azure.search.documents.indexes.models.SearchServiceStatistics;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ServiceStatistics} and
 * {@link SearchServiceStatistics}.
 */
public final class ServiceStatisticsConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.ServiceStatistics} to
     * {@link SearchServiceStatistics}.
     */
    public static SearchServiceStatistics map(com.azure.search.documents.indexes.implementation.models.ServiceStatistics obj) {
        if (obj == null) {
            return null;
        }

        ServiceCounters counters = null;
        if (obj.getCounters() != null) {
            counters = ServiceCountersConverter.map(obj.getCounters());
        }

        ServiceLimits limits = null;
        if (obj.getLimits() != null) {
            limits = ServiceLimitsConverter.map(obj.getLimits());
        }

        return new SearchServiceStatistics(counters, limits);
    }

    /**
     * Maps from {@link SearchServiceStatistics} to
     * {@link com.azure.search.documents.indexes.implementation.models.ServiceStatistics}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ServiceStatistics map(SearchServiceStatistics obj) {
        if (obj == null) {
            return null;
        }

        com.azure.search.documents.indexes.implementation.models.ServiceCounters counters =
            obj.getCounters() == null ? null
            : ServiceCountersConverter.map(obj.getCounters());

        com.azure.search.documents.indexes.implementation.models.ServiceLimits limits =
            obj.getLimits() == null ? null
            : ServiceLimitsConverter.map(obj.getLimits());
        com.azure.search.documents.indexes.implementation.models.ServiceStatistics serviceStatistics =
            new com.azure.search.documents.indexes.implementation.models.ServiceStatistics(counters, limits);
        serviceStatistics.validate();
        return serviceStatistics;
    }

    private ServiceStatisticsConverter() {
    }
}
