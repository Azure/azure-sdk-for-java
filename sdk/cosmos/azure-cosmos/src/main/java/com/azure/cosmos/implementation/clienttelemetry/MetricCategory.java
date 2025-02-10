// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import java.util.EnumSet;
import java.util.Locale;

public enum MetricCategory {
    OperationSummary("OperationSummary", 1 << 0),
    OperationDetails("OperationDetails", 1 << 1),
    RequestSummary("RequestSummary", 1 << 2),
    RequestDetails("RequestDetails", 1 << 3),
    AddressResolutions("AddressResolutions", 1 << 4),
    DirectChannels("DirectChannels", 1 << 5),
    DirectEndpoints("DirectEndpoints", 1 << 6),
    DirectRequests("DirectRequests", 1 << 7),
    System("System", 1 << 8),
    Legacy("Legacy", 1 << 9);

    private final int value;
    private final String stringValue;
    private final String toLowerStringValue;

    MetricCategory(String stringValue, int value) {
        this.stringValue = stringValue;
        this.value = value;
        this.toLowerStringValue = stringValue.toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return this.stringValue;
    }

    public String toLowerCase() {
        return this.toLowerStringValue;
    }

    public int value() {
        return this.value;
    }

    public static final EnumSet<MetricCategory> ALL_CATEGORIES = EnumSet.allOf(MetricCategory.class);

    public static final EnumSet<MetricCategory> DEFAULT_CATEGORIES = EnumSet.of(
        MetricCategory.OperationSummary,
        MetricCategory.RequestSummary,
        MetricCategory.DirectRequests,
        MetricCategory.DirectChannels,
        MetricCategory.System
    );

    public static final EnumSet<MetricCategory> MINIMAL_CATEGORIES = EnumSet.of(
        MetricCategory.OperationSummary,
        MetricCategory.System
    );

    public static MetricCategory fromValue(String value) {
        for (MetricCategory metricCategory : MetricCategory.values()) {
            if (metricCategory.toLowerStringValue.equalsIgnoreCase(value)) {
                return metricCategory;
            }
        }
        return null;
    }
}
