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
    private String timex;

    /*
     * The DateTime SubKind
     */
    private DateTimeSubKind dateTimeSubKind;

    /*
     * The actual time that the extracted text denote.
     */
    private String value;

    /*
     * An optional modifier of a date/time instance.
     */
    private TemporalModifier modifier;

    static {
        DateTimeResolutionPropertiesHelper.setAccessor(
            new DateTimeResolutionPropertiesHelper.DateTimeResolutionAccessor() {
                @Override
                public void setTimex(DateTimeResolution dateTimeResolution, String timex) {
                    dateTimeResolution.setTimex(timex);
                }

                @Override
                public void setDateTimeSubKind(DateTimeResolution dateTimeResolution, DateTimeSubKind dateTimeSubKind) {
                    dateTimeResolution.setDateTimeSubKind(dateTimeSubKind);
                }

                @Override
                public void setValue(DateTimeResolution dateTimeResolution, String value) {
                    dateTimeResolution.setValue(value);
                }

                @Override
                public void setModifier(DateTimeResolution dateTimeResolution, TemporalModifier temporalModifier) {
                    dateTimeResolution.setModifier(temporalModifier);
                }
            });
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

    private void setTimex(String timex) {
        this.timex = timex;
    }

    private void setDateTimeSubKind(DateTimeSubKind dateTimeSubKind) {
        this.dateTimeSubKind = dateTimeSubKind;
    }

    private void setValue(String value) {
        this.value = value;
    }

    private void setModifier(TemporalModifier modifier) {
        this.modifier = modifier;
    }
}
