// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.core.annotation.Fluent;

/** The KeyPhraseExtractionTaskState model. */
@Fluent
public final class KeyPhraseExtractionTaskState extends TaskState {
    /*
     * The results property.
     */
    private ExtractKeyPhrasesResultCollection results;

    /**
     * Get the results property: The results property.
     *
     * @return the results value.
     */
    public ExtractKeyPhrasesResultCollection getResults() {
        return this.results;
    }

    /**
     * Set the results property: The results property.
     *
     * @param results the results value to set.
     * @return the TasksStateTasksKeyPhraseExtractionTasksItem object itself.
     */
    public KeyPhraseExtractionTaskState setResults(ExtractKeyPhrasesResultCollection results) {
        this.results = results;
        return this;
    }
}
