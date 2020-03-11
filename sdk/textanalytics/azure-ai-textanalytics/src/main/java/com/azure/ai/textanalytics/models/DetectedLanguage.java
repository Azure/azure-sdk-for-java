// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The {@link DetectedLanguage} model.
 */
@Immutable
public final class DetectedLanguage {
    /*
     * Long name of a detected language (e.g. English, French).
     */
    private final String name;

    /*
     * A two letter representation of the detected language according to the
     * ISO 639-1 standard (e.g. en, fr).
     */
    private final String iso6391Name;

    /*
     * A confidence score between 0 and 1. Scores close to 1 indicate 100%
     * certainty that the identified language is true.
     */
    private final double score;

    /**
     * Creates a {@link DetectedLanguage} model that describes detected language content.
     *
     * @param name The name of a detected language.
     * @param iso6391Name A two letter representation of the detected language according to the ISO 639-1 standard.
     * @param score A confidence score between 0 and 1.
     */
    public DetectedLanguage(String name, String iso6391Name, double score) {
        this.name = name;
        this.iso6391Name = iso6391Name;
        this.score = score;
    }

    /**
     * Get the name property: Long name of a detected language (e.g. English, French).
     *
     * @return The name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the iso6391Name property: A two letter representation of the
     * detected language according to the ISO 639-1 standard (e.g. en, fr).
     *
     * @return The iso6391Name value.
     */
    public String getIso6391Name() {
        return this.iso6391Name;
    }

    /**
     * Get the score property: A confidence score between 0 and 1. Scores close
     * to 1 indicate 100% certainty that the identified language is true.
     *
     * @return The score value.
     */
    public double getScore() {
        return this.score;
    }
}
