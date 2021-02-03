// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.Arrays;

/**
 * The {@link TextAnalyticsActions} model.
 */
@Fluent
public final class TextAnalyticsActions {
    private String displayName;
    private Iterable<RecognizeEntitiesOptions> recognizeEntitiesOptions;
    private Iterable<RecognizePiiEntitiesOptions> recognizePiiEntitiesOptions;
    private Iterable<ExtractKeyPhrasesOptions> extractKeyPhrasesOptions;

    /**
     * Get the custom name for the actions.
     *
     * @return the custom name for the actions.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set the custom name for the actions.
     *
     * @param displayName the custom name for the actions.
     *
     * @return the {@link TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the list of {@link RecognizeEntitiesOptions} to be executed.
     *
     * @return the list of {@link RecognizeEntitiesOptions} to be executed.
     */
    public Iterable<RecognizeEntitiesOptions> getRecognizeEntitiesOptions() {
        return this.recognizeEntitiesOptions;
    }

    /**
     * Set the list of {@link RecognizeEntitiesOptions} to be executed.
     *
     * @param recognizeEntitiesOptions the list of {@link RecognizeEntitiesOptions} to be executed.
     *
     * @return the AnalyzeTasksOptions object itself.
     */
    public TextAnalyticsActions setRecognizeEntitiesOptions(RecognizeEntitiesOptions... recognizeEntitiesOptions) {
        this.recognizeEntitiesOptions = Arrays.asList(recognizeEntitiesOptions);
        return this;
    }

    /**
     * Get the list of {@link RecognizePiiEntitiesOptions} to be executed.
     *
     * @return the list of {@link RecognizePiiEntitiesOptions} to be executed.
     */
    public Iterable<RecognizePiiEntitiesOptions> getRecognizePiiEntitiesOptions() {
        return this.recognizePiiEntitiesOptions;
    }

    /**
     * Set the list of {@link RecognizePiiEntitiesOptions} to be executed.
     *
     * @param recognizePiiEntitiesOptions the list of {@link RecognizePiiEntitiesOptions} to be executed.
     *
     * @return the AnalyzeTasksOptions object itself.
     */
    public TextAnalyticsActions setRecognizePiiEntitiesOptions(RecognizePiiEntitiesOptions... recognizePiiEntitiesOptions) {
        this.recognizePiiEntitiesOptions = Arrays.asList(recognizePiiEntitiesOptions);
        return this;
    }

    /**
     * Get the list of {@link ExtractKeyPhrasesOptions} to be executed.
     *
     * @return the list of {@link ExtractKeyPhrasesOptions} to be executed.
     */
    public Iterable<ExtractKeyPhrasesOptions> getExtractKeyPhrasesOptions() {
        return this.extractKeyPhrasesOptions;
    }

    /**
     * Set the list of {@link ExtractKeyPhrasesOptions} to be executed.
     *
     * @param extractKeyPhrasesOptions the list of {@link ExtractKeyPhrasesOptions} to be executed.
     *
     * @return the AnalyzeTasksOptions object itself.
     */
    public TextAnalyticsActions setExtractKeyPhrasesOptions(ExtractKeyPhrasesOptions... extractKeyPhrasesOptions) {
        this.extractKeyPhrasesOptions = Arrays.asList(extractKeyPhrasesOptions);
        return this;
    }
}
