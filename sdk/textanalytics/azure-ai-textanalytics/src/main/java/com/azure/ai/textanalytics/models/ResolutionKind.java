// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/** Defines values for ResolutionKind. */
public enum ResolutionKind {
    /** Enum value AgeResolution. */
    AGE_RESOLUTION("AgeResolution"),

    /** Enum value AreaResolution. */
    AREA_RESOLUTION("AreaResolution"),

    /** Enum value CurrencyResolution. */
    CURRENCY_RESOLUTION("CurrencyResolution"),

    /** Enum value DateTimeResolution. */
    DATE_TIME_RESOLUTION("DateTimeResolution"),

    /** Enum value InformationResolution. */
    INFORMATION_RESOLUTION("InformationResolution"),

    /** Enum value LengthResolution. */
    LENGTH_RESOLUTION("LengthResolution"),

    /** Enum value NumberResolution. */
    NUMBER_RESOLUTION("NumberResolution"),

    /** Enum value NumericRangeResolution. */
    NUMERIC_RANGE_RESOLUTION("NumericRangeResolution"),

    /** Enum value OrdinalResolution. */
    ORDINAL_RESOLUTION("OrdinalResolution"),

    /** Enum value SpeedResolution. */
    SPEED_RESOLUTION("SpeedResolution"),

    /** Enum value TemporalSpanResolution. */
    TEMPORAL_SPAN_RESOLUTION("TemporalSpanResolution"),

    /** Enum value TemperatureResolution. */
    TEMPERATURE_RESOLUTION("TemperatureResolution"),

    /** Enum value VolumeResolution. */
    VOLUME_RESOLUTION("VolumeResolution"),

    /** Enum value WeightResolution. */
    WEIGHT_RESOLUTION("WeightResolution");

    /** The actual serialized value for a ResolutionKind instance. */
    private final String value;

    ResolutionKind(String value) {
        this.value = value;
    }

    /**
     * Get the enum value in string.
     *
     * @return the enum value in string.
     */
    public String getValue() {
        return value;
    }
}
