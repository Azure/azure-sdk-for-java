// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.DateTimeResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** A resolution for datetime entity instances. */
@Immutable
public final class DateTimeResolution extends BaseResolution {
    /*
     * An extended ISO 8601 date/time representation as described in
     * (https://github.com/Microsoft/Recognizers-Text/blob/master/Patterns/English/English-DateTime.yaml)
     */
    private final String timex;

    /*
     * The DateTime SubKind
     */
    private final DateTimeSubKind dateTimeSubKind;

    /*
     * The actual time that the extracted text denote.
     */
    private final String value;

    /*
     * An optional modifier of a date/time instance.
     */
    private TemporalModifier modifier;

    /**
     * Create a resolution for datetime entity instances.
     *
     * @param timex An extended ISO 8601 date/time representation as described in
     *   (https://github.com/Microsoft/Recognizers-Text/blob/master/Patterns/English/English-DateTime.yaml).
     * @param dateTimeSubKind The DateTime SubKind.
     * @param value The actual time that the extracted text denote.
     */
    public DateTimeResolution(String timex, DateTimeSubKind dateTimeSubKind, String value) {
        this.timex = timex;
        this.dateTimeSubKind = dateTimeSubKind;
        this.value = value;
    }

    static {
        DateTimeResolutionPropertiesHelper.setAccessor(
                (dateTimeResolution, temporalModifier) -> dateTimeResolution.setModifier(temporalModifier));
    }

    /**
     * Get the timex property: An extended ISO 8601 date/time representation as described in
     * (https://github.com/Microsoft/Recognizers-Text/blob/master/Patterns/English/English-DateTime.yaml).
     *
     * @return the timex value.
     */
    public String getTimex() {
        return this.timex;
    }

    /**
     * Get the dateTimeSubKind property: The DateTime SubKind.
     *
     * @return the dateTimeSubKind value.
     */
    public DateTimeSubKind getDateTimeSubKind() {
        return this.dateTimeSubKind;
    }

    /**
     * Get the value property: The actual time that the extracted text denote.
     *
     * @return the value value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Get the modifier property: An optional modifier of a date/time instance.
     *
     * @return the modifier value.
     */
    public TemporalModifier getModifier() {
        return this.modifier;
    }

    /**
     * Set the modifier property: An optional modifier of a date/time instance.
     *
     * @param modifier the modifier value to set.
     * @return the DateTimeResolution object itself.
     */
    private void setModifier(TemporalModifier modifier) {
        this.modifier = modifier;
    }
}
