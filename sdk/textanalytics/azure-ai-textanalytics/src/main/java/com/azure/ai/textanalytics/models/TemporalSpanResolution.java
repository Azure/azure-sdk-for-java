// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.TemporalSpanResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** represents the resolution of a date and/or time span. */
@Immutable
public final class TemporalSpanResolution extends BaseResolution {
    /*
     * An extended ISO 8601 date/time representation as described in
     * (https://github.com/Microsoft/Recognizers-Text/blob/master/Patterns/English/English-DateTime.yaml)
     */
    private String begin;

    /*
     * An extended ISO 8601 date/time representation as described in
     * (https://github.com/Microsoft/Recognizers-Text/blob/master/Patterns/English/English-DateTime.yaml)
     */
    private String end;

    /*
     * An optional duration value formatted based on the ISO 8601 (https://en.wikipedia.org/wiki/ISO_8601#Durations)
     */
    private String duration;

    /*
     * An optional modifier of a date/time instance.
     */
    private TemporalModifier modifier;

    /*
     * An optional triplet containing the beginning, the end, and the duration all stated as ISO 8601 formatted
     * strings.
     */
    private String timex;

    static {
        TemporalSpanResolutionPropertiesHelper.setAccessor(
            new TemporalSpanResolutionPropertiesHelper.TemporalSpanResolutionAccessor() {
                @Override
                public void setBegin(TemporalSpanResolution temporalSpanResolution, String begin) {
                    temporalSpanResolution.setBegin(begin);
                }

                @Override
                public void setEnd(TemporalSpanResolution temporalSpanResolution, String end) {
                    temporalSpanResolution.setEnd(end);
                }

                @Override
                public void setDuration(TemporalSpanResolution temporalSpanResolution, String duration) {
                    temporalSpanResolution.setDuration(duration);
                }

                @Override
                public void setModifier(TemporalSpanResolution temporalSpanResolution, TemporalModifier modifier) {
                    temporalSpanResolution.setModifier(modifier);
                }

                @Override
                public void setTimex(TemporalSpanResolution temporalSpanResolution, String timex) {
                    temporalSpanResolution.setTimex(timex);
                }
            });
    }

    /**
     * Get the begin property: An extended ISO 8601 date/time representation as described in
     * (https://github.com/Microsoft/Recognizers-Text/blob/master/Patterns/English/English-DateTime.yaml).
     *
     * @return the begin value.
     */
    public String getBegin() {
        return this.begin;
    }

    /**
     * Get the end property: An extended ISO 8601 date/time representation as described in
     * (https://github.com/Microsoft/Recognizers-Text/blob/master/Patterns/English/English-DateTime.yaml).
     *
     * @return the end value.
     */
    public String getEnd() {
        return this.end;
    }

    /**
     * Get the duration property: An optional duration value formatted based on the ISO 8601
     * (https://en.wikipedia.org/wiki/ISO_8601#Durations).
     *
     * @return the duration value.
     */
    public String getDuration() {
        return this.duration;
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
     * Get the timex property: An optional triplet containing the beginning, the end, and the duration all stated as ISO
     * 8601 formatted strings.
     *
     * @return the timex value.
     */
    public String getTimex() {
        return this.timex;
    }

    private void setBegin(String begin) {
        this.begin = begin;
    }

    private void setEnd(String end) {
        this.end = end;
    }

    private void setDuration(String duration) {
        this.duration = duration;
    }

    private void setModifier(TemporalModifier modifier) {
        this.modifier = modifier;
    }

    private void setTimex(String timex) {
        this.timex = timex;
    }
}
