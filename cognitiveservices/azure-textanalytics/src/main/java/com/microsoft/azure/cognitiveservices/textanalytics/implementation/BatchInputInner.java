/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.textanalytics.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.textanalytics.Input;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The BatchInputInner model.
 */
public class BatchInputInner {
    /**
     * The documents property.
     */
    @JsonProperty(value = "documents")
    private List<Input> documents;

    /**
     * Get the documents value.
     *
     * @return the documents value
     */
    public List<Input> documents() {
        return this.documents;
    }

    /**
     * Set the documents value.
     *
     * @param documents the documents value to set
     * @return the BatchInputInner object itself.
     */
    public BatchInputInner withDocuments(List<Input> documents) {
        this.documents = documents;
        return this;
    }

}
