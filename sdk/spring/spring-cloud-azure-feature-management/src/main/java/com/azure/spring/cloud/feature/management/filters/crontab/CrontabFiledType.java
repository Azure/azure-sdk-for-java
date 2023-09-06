// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters.crontab;

public enum CrontabFiledType {
    MINUTE("minute"),
    HOUR("hour"),
    DAY_OF_MONTH("day of month"),
    MONTH("month"),
    DAY_OF_WEEK("day of week");

    private final String description;

    CrontabFiledType(final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
