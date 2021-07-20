// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link DetectedLanguage} model.
 */
@Immutable
public final class DetectedLanguage {
    /**
     * Long name of a detected language (e.g. English, French).
     */
    private final String name;

    /**
     * A two letter representation of the detected language according to the
     * ISO 639-1 standard (e.g. en, fr).
     */
    private final String iso6391Name;

    /**
     * A confidence score between 0 and 1. Scores close to 1 indicate 100%
     * certainty that the identified language is true.
     */
    private final double confidenceScore;

    /**
     * An {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     */
    private final IterableStream<TextAnalyticsWarning> warnings;

    /**
     * Creates a {@link DetectedLanguage} model that describes detected language content.
     *
     * @param name The name of a detected language.
     * @param iso6391Name A two letter representation of the detected language according to the ISO 639-1 standard.
     * @param confidenceScore A confidence score between 0 and 1.
     * @param warnings An {@link IterableStream} of {@link TextAnalyticsWarning warnings}.
     */
    public DetectedLanguage(String name, String iso6391Name, double confidenceScore,
                            IterableStream<TextAnalyticsWarning> warnings) {
        this.name = name;
        this.iso6391Name = iso6391Name;
        this.confidenceScore = confidenceScore;
        this.warnings = warnings;
    }

    /**
     * Gets the name property: Long name of a detected language (e.g. English,
     * French).
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the iso6391Name property: A two letter representation of the
     * detected language according to the ISO 639-1 standard (e.g. en, fr).
     *
     * @return the iso6391Name value.
     */
    public String getIso6391Name() {
        return this.iso6391Name;
    }

    /**
     * Gets the confidenceScore property: A confidence score between 0 and 1.
     * Scores close to 1 indicate 100% certainty that the identified language
     * is true.
     *
     * @return the confidenceScore value.
     */
    public double getConfidenceScore() {
        return this.confidenceScore;
    }

    /**
     * Gets the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }
}
