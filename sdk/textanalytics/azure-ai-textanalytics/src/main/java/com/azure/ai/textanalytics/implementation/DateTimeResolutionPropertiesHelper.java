// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.DateTimeResolution;
import com.azure.ai.textanalytics.models.DateTimeSubKind;
import com.azure.ai.textanalytics.models.TemporalModifier;

public final class DateTimeResolutionPropertiesHelper {
    private static DateTimeResolutionAccessor accessor;

    private DateTimeResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link DateTimeResolution} instance.
     */
    public interface DateTimeResolutionAccessor {
        void setTimex(DateTimeResolution dateTimeResolution, String timex);
        void setDateTimeSubKind(DateTimeResolution dateTimeResolution, DateTimeSubKind dateTimeSubKind);
        void setValue(DateTimeResolution dateTimeResolution, String value);
        void setModifier(DateTimeResolution dateTimeResolution, TemporalModifier temporalModifier);
    }

    /**
     * The method called from {@link DateTimeResolution} to set it's accessor.
     *
     * @param dateTimeResolutionAccessor The accessor.
     */
    public static void setAccessor(final DateTimeResolutionAccessor dateTimeResolutionAccessor) {
        accessor = dateTimeResolutionAccessor;
    }

    public static void setTimex(DateTimeResolution dateTimeResolution, String timex) {
        accessor.setTimex(dateTimeResolution, timex);
    }

    public static void setDateTimeSubKind(DateTimeResolution dateTimeResolution, DateTimeSubKind dateTimeSubKind) {
        accessor.setDateTimeSubKind(dateTimeResolution, dateTimeSubKind);
    }

    public static void setValue(DateTimeResolution dateTimeResolution, String value) {
        accessor.setValue(dateTimeResolution, value);
    }

    public static void setModifier(DateTimeResolution dateTimeResolution, TemporalModifier temporalModifier) {
        accessor.setModifier(dateTimeResolution, temporalModifier);
    }
}
