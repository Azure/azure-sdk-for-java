// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/**
 * The {@link DetectedLanguage} model.
 */
public interface DetectedLanguage {
    /**
     * Get the name property: Long name of a detected language (e.g. English, French).
     *
     * @return The name value.
     */
    String getName();

    /**
     * Get the iso6391Name property: A two letter representation of the
     * detected language according to the ISO 639-1 standard (e.g. en, fr).
     *
     * @return The iso6391Name value.
     */
    String getIso6391Name();

    /**
     * Get the score property: A confidence score between 0 and 1. Scores close
     * to 1 indicate 100% certainty that the identified language is true.
     *
     * @return The score value.
     */
    double getConfidenceScore();
}
