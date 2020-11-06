// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/** The JobManifestTasks model. */
@Fluent
public final class JobManifestTasks {
    /*
     * The entityRecognitionTasks property.
     */
    private List<EntitiesTask> entityRecognitionTasks;

    /*
     * The entityRecognitionPiiTasks property.
     */
    private List<PiiTask> entityRecognitionPiiTasks;

    /*
     * The keyPhraseExtractionTasks property.
     */
    private List<KeyPhrasesTask> keyPhraseExtractionTasks;

    /**
     * Get the entityRecognitionTasks property: The entityRecognitionTasks property.
     *
     * @return the entityRecognitionTasks value.
     */
    public List<EntitiesTask> getEntityRecognitionTasks() {
        return this.entityRecognitionTasks;
    }

    /**
     * Set the entityRecognitionTasks property: The entityRecognitionTasks property.
     *
     * @param entityRecognitionTasks the entityRecognitionTasks value to set.
     * @return the JobManifestTasks object itself.
     */
    public JobManifestTasks setEntityRecognitionTasks(List<EntitiesTask> entityRecognitionTasks) {
        this.entityRecognitionTasks = entityRecognitionTasks;
        return this;
    }

    /**
     * Get the entityRecognitionPiiTasks property: The entityRecognitionPiiTasks property.
     *
     * @return the entityRecognitionPiiTasks value.
     */
    public List<PiiTask> getEntityRecognitionPiiTasks() {
        return this.entityRecognitionPiiTasks;
    }

    /**
     * Set the entityRecognitionPiiTasks property: The entityRecognitionPiiTasks property.
     *
     * @param entityRecognitionPiiTasks the entityRecognitionPiiTasks value to set.
     * @return the JobManifestTasks object itself.
     */
    public JobManifestTasks setEntityRecognitionPiiTasks(List<PiiTask> entityRecognitionPiiTasks) {
        this.entityRecognitionPiiTasks = entityRecognitionPiiTasks;
        return this;
    }

    /**
     * Get the keyPhraseExtractionTasks property: The keyPhraseExtractionTasks property.
     *
     * @return the keyPhraseExtractionTasks value.
     */
    public List<KeyPhrasesTask> getKeyPhraseExtractionTasks() {
        return this.keyPhraseExtractionTasks;
    }

    /**
     * Set the keyPhraseExtractionTasks property: The keyPhraseExtractionTasks property.
     *
     * @param keyPhraseExtractionTasks the keyPhraseExtractionTasks value to set.
     * @return the JobManifestTasks object itself.
     */
    public JobManifestTasks setKeyPhraseExtractionTasks(List<KeyPhrasesTask> keyPhraseExtractionTasks) {
        this.keyPhraseExtractionTasks = keyPhraseExtractionTasks;
        return this;
    }
}
