// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.DateTimeResolution;
import com.azure.ai.textanalytics.models.TemporalModifier;

public final class DateTimeResolutionPropertiesHelper {
    private static DateTimeResolutionAccessor accessor;

    private DateTimeResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link DateTimeResolution} instance.
     */
    public interface DateTimeResolutionAccessor {
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

    public static void setModifier(DateTimeResolution dateTimeResolution, TemporalModifier temporalModifier) {
        accessor.setModifier(dateTimeResolution, temporalModifier);
    }
}
