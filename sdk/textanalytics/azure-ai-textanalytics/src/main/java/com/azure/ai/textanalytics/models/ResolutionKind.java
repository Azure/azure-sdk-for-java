// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for ResolutionKind. */
public final class ResolutionKind extends ExpandableStringEnum<ResolutionKind> {
    /** Static value DateTimeResolution for ResolutionKind. */
    public static final ResolutionKind DATE_TIME_RESOLUTION = fromString("DateTimeResolution");

    /** Static value NumberResolution for ResolutionKind. */
    public static final ResolutionKind NUMBER_RESOLUTION = fromString("NumberResolution");

    /** Static value OrdinalResolution for ResolutionKind. */
    public static final ResolutionKind ORDINAL_RESOLUTION = fromString("OrdinalResolution");

    /** Static value SpeedResolution for ResolutionKind. */
    public static final ResolutionKind SPEED_RESOLUTION = fromString("SpeedResolution");

    /** Static value WeightResolution for ResolutionKind. */
    public static final ResolutionKind WEIGHT_RESOLUTION = fromString("WeightResolution");

    /** Static value LengthResolution for ResolutionKind. */
    public static final ResolutionKind LENGTH_RESOLUTION = fromString("LengthResolution");

    /** Static value VolumeResolution for ResolutionKind. */
    public static final ResolutionKind VOLUME_RESOLUTION = fromString("VolumeResolution");

    /** Static value AreaResolution for ResolutionKind. */
    public static final ResolutionKind AREA_RESOLUTION = fromString("AreaResolution");

    /** Static value AgeResolution for ResolutionKind. */
    public static final ResolutionKind AGE_RESOLUTION = fromString("AgeResolution");

    /** Static value InformationResolution for ResolutionKind. */
    public static final ResolutionKind INFORMATION_RESOLUTION = fromString("InformationResolution");

    /** Static value TemperatureResolution for ResolutionKind. */
    public static final ResolutionKind TEMPERATURE_RESOLUTION = fromString("TemperatureResolution");

    /** Static value CurrencyResolution for ResolutionKind. */
    public static final ResolutionKind CURRENCY_RESOLUTION = fromString("CurrencyResolution");

    /** Static value NumericRangeResolution for ResolutionKind. */
    public static final ResolutionKind NUMERIC_RANGE_RESOLUTION = fromString("NumericRangeResolution");

    /** Static value TemporalSpanResolution for ResolutionKind. */
    public static final ResolutionKind TEMPORAL_SPAN_RESOLUTION = fromString("TemporalSpanResolution");

    /**
     * Creates or finds a ResolutionKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ResolutionKind.
     */
    public static ResolutionKind fromString(String name) {
        return fromString(name, ResolutionKind.class);
    }
}
