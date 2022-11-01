// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TemporalModifier;
import com.azure.ai.textanalytics.models.TemporalSpanResolution;

public final class TemporalSpanResolutionPropertiesHelper {
    private static TemporalSpanResolutionAccessor accessor;

    private TemporalSpanResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link TemporalSpanResolution} instance.
     */
    public interface TemporalSpanResolutionAccessor {
        void setBegin(TemporalSpanResolution temporalSpanResolution, String begin);
        void setEnd(TemporalSpanResolution temporalSpanResolution, String end);
        void setDuration(TemporalSpanResolution temporalSpanResolution, String duration);
        void setModifier(TemporalSpanResolution temporalSpanResolution, TemporalModifier modifier);
        void setTimex(TemporalSpanResolution temporalSpanResolution, String timex);
    }

    /**
     * The method called from {@link TemporalSpanResolution} to set it's accessor.
     *
     * @param temporalSpanResolutionAccessor The accessor.
     */
    public static void setAccessor(final TemporalSpanResolutionAccessor temporalSpanResolutionAccessor) {
        accessor = temporalSpanResolutionAccessor;
    }

    public static void setBegin(TemporalSpanResolution temporalSpanResolution, String begin) {
        accessor.setBegin(temporalSpanResolution, begin);
    }

    public static void setEnd(TemporalSpanResolution temporalSpanResolution, String end) {
        accessor.setEnd(temporalSpanResolution, end);
    }

    public static void setDuration(TemporalSpanResolution temporalSpanResolution, String duration) {
        accessor.setDuration(temporalSpanResolution, duration);
    }

    public static void setModifier(TemporalSpanResolution temporalSpanResolution, TemporalModifier modifier) {
        accessor.setModifier(temporalSpanResolution, modifier);
    }

    public static void setTimex(TemporalSpanResolution temporalSpanResolution, String timex) {
        accessor.setTimex(temporalSpanResolution, timex);
    }
}
