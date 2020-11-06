// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.annotation.Fluent;

/** The TasksStateTasksEntityRecognitionPiiTasksItem model. */
@Fluent
public final class PiiEntityRecognitionTaskState extends TaskState {
    /*
     * The results property.
     */
    private RecognizePiiEntitiesResultCollection results;

    /**
     * Get the results property: The results property.
     *
     * @return the results value.
     */
    public RecognizePiiEntitiesResultCollection getResults() {
        return this.results;
    }

    /**
     * Set the results property: The results property.
     *
     * @param results the results value to set.
     * @return the TasksStateTasksEntityRecognitionPiiTasksItem object itself.
     */
    public PiiEntityRecognitionTaskState setResults(RecognizePiiEntitiesResultCollection results) {
        this.results = results;
        return this;
    }
}
