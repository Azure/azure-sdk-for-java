/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.textanalytics.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.textanalytics.SentimentBatchResultItem;
import com.microsoft.azure.cognitiveservices.textanalytics.ErrorRecord;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The SentimentBatchResultInner model.
 */
public class SentimentBatchResultInner {
    /**
     * The documents property.
     */
    @JsonProperty(value = "documents", access = JsonProperty.Access.WRITE_ONLY)
    private List<SentimentBatchResultItem> documents;

    /**
     * The errors property.
     */
    @JsonProperty(value = "errors", access = JsonProperty.Access.WRITE_ONLY)
    private List<ErrorRecord> errors;

    /**
     * Get the documents value.
     *
     * @return the documents value
     */
    public List<SentimentBatchResultItem> documents() {
        return this.documents;
    }

    /**
     * Get the errors value.
     *
     * @return the errors value
     */
    public List<ErrorRecord> errors() {
        return this.errors;
    }

}
