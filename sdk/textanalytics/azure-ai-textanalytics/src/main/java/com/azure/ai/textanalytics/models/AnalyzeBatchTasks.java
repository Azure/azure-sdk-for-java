// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.Arrays;

/**
 * The {@link AnalyzeBatchTasks} model.
 */
@Fluent
public final class AnalyzeBatchTasks {
    private Iterable<RecognizeEntityOptions> recognizeEntityOptions;
    private Iterable<RecognizePiiEntityOptions> recognizePiiEntityOptions;
    private Iterable<ExtractKeyPhrasesOptions> extractKeyPhraseOptions;

    /**
     * Get the list of {@link RecognizeEntityOptions} to be analyzed.
     *
     * @return the categorizedEntitiesRecognitionTasks value.
     */
    public Iterable<RecognizeEntityOptions> getRecognizeEntityOptions() {
        return this.recognizeEntityOptions;
    }

    /**
     * Set the list of {@link RecognizeEntityOptions} to be analyzed.
     *
     * @param recognizeEntityOptions the list of {@link RecognizeEntityOptions} to be analyzed.
     *
     * @return the AnalyzeTasksOptions object itself.
     */
    public AnalyzeBatchTasks setRecognizeEntityOptions(
        RecognizeEntityOptions... recognizeEntityOptions) {
        this.recognizeEntityOptions = Arrays.asList(recognizeEntityOptions);
        return this;
    }

    /**
     * Get the list of {@link RecognizePiiEntityOptions} to be analyzed.
     *
     * @return the list of {@link RecognizePiiEntityOptions} to be analyzed.
     */
    public Iterable<RecognizePiiEntityOptions> getRecognizePiiEntityOptions() {
        return this.recognizePiiEntityOptions;
    }

    /**
     * Set the list of {@link RecognizePiiEntityOptions} to be analyzed.
     *
     * @param recognizePiiEntityOptions the list of {@link RecognizePiiEntityOptions} to be analyzed.
     *
     * @return the AnalyzeTasksOptions object itself.
     */
    public AnalyzeBatchTasks setRecognizePiiEntityOptions(RecognizePiiEntityOptions... recognizePiiEntityOptions) {
        this.recognizePiiEntityOptions = Arrays.asList(recognizePiiEntityOptions);
        return this;
    }

    /**
     * Get the list of {@link ExtractKeyPhrasesOptions} to be analyzed.
     *
     * @return the list of {@link ExtractKeyPhrasesOptions} to be analyzed.
     */
    public Iterable<ExtractKeyPhrasesOptions> getExtractKeyPhraseOptions() {
        return this.extractKeyPhraseOptions;
    }

    /**
     * Set the list of {@link ExtractKeyPhrasesOptions} to be analyzed.
     *
     * @param extractKeyPhraseOptions the list of {@link ExtractKeyPhrasesOptions} to be analyzed.
     *
     * @return the AnalyzeTasksOptions object itself.
     */
    public AnalyzeBatchTasks setExtractKeyPhraseOptions(ExtractKeyPhrasesOptions... extractKeyPhraseOptions) {
        this.extractKeyPhraseOptions = Arrays.asList(extractKeyPhraseOptions);
        return this;
    }
}
