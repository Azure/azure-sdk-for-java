/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.language.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The BatchInput model.
 */
public class BatchInput {
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
     * @return the BatchInput object itself.
     */
    public BatchInput withDocuments(List<Input> documents) {
        this.documents = documents;
        return this;
    }

}
