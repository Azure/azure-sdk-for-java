/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.textanalytics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The SentimentBatchResultItem model.
 */
public class SentimentBatchResultItem {
    /**
     * A decimal number between 0 and 1 denoting the sentiment of the document.
     * A score above 0.7 usually refers to a positive document while a score
     * below 0.3 normally has a negative connotation. Mid values refer to
     * neutral text.
     */
    @JsonProperty(value = "score", access = JsonProperty.Access.WRITE_ONLY)
    private Double score;

    /**
     * Unique document identifier.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /**
     * Get the score value.
     *
     * @return the score value
     */
    public Double score() {
        return this.score;
    }

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

}
