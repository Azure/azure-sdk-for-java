// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for DateTimeSubKind. */
public final class DateTimeSubKind extends ExpandableStringEnum<DateTimeSubKind> {
    /** Static value Time for DateTimeSubKind. */
    public static final DateTimeSubKind TIME = fromString("Time");

    /** Static value Date for DateTimeSubKind. */
    public static final DateTimeSubKind DATE = fromString("Date");

    /** Static value DateTime for DateTimeSubKind. */
    public static final DateTimeSubKind DATE_TIME = fromString("DateTime");

    /** Static value Duration for DateTimeSubKind. */
    public static final DateTimeSubKind DURATION = fromString("Duration");

    /** Static value Set for DateTimeSubKind. */
    public static final DateTimeSubKind SET = fromString("Set");

    /**
     * Creates or finds a DateTimeSubKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DateTimeSubKind.
     */
    public static DateTimeSubKind fromString(String name) {
        return fromString(name, DateTimeSubKind.class);
    }
}
