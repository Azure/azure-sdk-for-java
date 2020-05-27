// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ServiceCounters;
import com.azure.search.documents.indexes.models.ServiceLimits;
import com.azure.search.documents.indexes.models.ServiceStatistics;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ServiceStatistics} and
 * {@link ServiceStatistics}.
 */
public final class ServiceStatisticsConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.ServiceStatistics} to
     * {@link ServiceStatistics}.
     */
    public static ServiceStatistics map(com.azure.search.documents.indexes.implementation.models.ServiceStatistics obj) {
        if (obj == null) {
            return null;
        }
        ServiceStatistics serviceStatistics = new ServiceStatistics();

        if (obj.getCounters() != null) {
            ServiceCounters counters = ServiceCountersConverter.map(obj.getCounters());
            serviceStatistics.setCounters(counters);
        }

        if (obj.getLimits() != null) {
            ServiceLimits limits = ServiceLimitsConverter.map(obj.getLimits());
            serviceStatistics.setLimits(limits);
        }
        return serviceStatistics;
    }

    /**
     * Maps from {@link ServiceStatistics} to
     * {@link com.azure.search.documents.indexes.implementation.models.ServiceStatistics}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ServiceStatistics map(ServiceStatistics obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.ServiceStatistics serviceStatistics =
            new com.azure.search.documents.indexes.implementation.models.ServiceStatistics();

        if (obj.getCounters() != null) {
            com.azure.search.documents.indexes.implementation.models.ServiceCounters counters =
                ServiceCountersConverter.map(obj.getCounters());
            serviceStatistics.setCounters(counters);
        }

        if (obj.getLimits() != null) {
            com.azure.search.documents.indexes.implementation.models.ServiceLimits limits =
                ServiceLimitsConverter.map(obj.getLimits());
            serviceStatistics.setLimits(limits);
        }
        return serviceStatistics;
    }

    private ServiceStatisticsConverter() {
    }
}
