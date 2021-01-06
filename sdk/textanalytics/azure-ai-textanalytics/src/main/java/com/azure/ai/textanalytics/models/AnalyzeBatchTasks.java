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
    private Iterable<CategorizedEntitiesRecognition> categorizedEntitiesRecognitions;
    private Iterable<PiiEntitiesRecognition> piiEntitiesRecognitions;
    private Iterable<KeyPhrasesExtraction> keyPhrasesExtractions;

    /**
     * Get the list of {@link CategorizedEntitiesRecognition} to be analyzed.
     *
     * @return the categorizedEntitiesRecognitionTasks value.
     */
    public Iterable<CategorizedEntitiesRecognition> getCategorizedEntitiesRecognitions() {
        return this.categorizedEntitiesRecognitions;
    }

    /**
     * Set the list of {@link CategorizedEntitiesRecognition} to be analyzed.
     *
     * @param categorizedEntitiesRecognitions the list of {@link CategorizedEntitiesRecognition} to be analyzed.
     *
     * @return the AnalyzeTasksOptions object itself.
     */
    public AnalyzeBatchTasks setCategorizedEntitiesRecognitions(
        CategorizedEntitiesRecognition... categorizedEntitiesRecognitions) {
        this.categorizedEntitiesRecognitions = Arrays.asList(categorizedEntitiesRecognitions);
        return this;
    }

    /**
     * Get the list of {@link PiiEntitiesRecognition} to be analyzed.
     *
     * @return the list of {@link PiiEntitiesRecognition} to be analyzed.
     */
    public Iterable<PiiEntitiesRecognition> getPiiEntitiesRecognitions() {
        return this.piiEntitiesRecognitions;
    }

    /**
     * Set the list of {@link PiiEntitiesRecognition} to be analyzed.
     *
     * @param piiEntitiesRecognitions the list of {@link PiiEntitiesRecognition} to be analyzed.
     *
     * @return the AnalyzeTasksOptions object itself.
     */
    public AnalyzeBatchTasks setPiiEntitiesRecognitions(PiiEntitiesRecognition... piiEntitiesRecognitions) {
        this.piiEntitiesRecognitions = Arrays.asList(piiEntitiesRecognitions);
        return this;
    }

    /**
     * Get the list of {@link KeyPhrasesExtraction} to be analyzed.
     *
     * @return the list of {@link KeyPhrasesExtraction} to be analyzed.
     */
    public Iterable<KeyPhrasesExtraction> getKeyPhrasesExtractions() {
        return this.keyPhrasesExtractions;
    }

    /**
     * Set the list of {@link KeyPhrasesExtraction} to be analyzed.
     *
     * @param keyPhrasesExtractions the list of {@link KeyPhrasesExtraction} to be analyzed.
     *
     * @return the AnalyzeTasksOptions object itself.
     */
    public AnalyzeBatchTasks setKeyPhrasesExtractions(KeyPhrasesExtraction... keyPhrasesExtractions) {
        this.keyPhrasesExtractions = Arrays.asList(keyPhrasesExtractions);
        return this;
    }
}
