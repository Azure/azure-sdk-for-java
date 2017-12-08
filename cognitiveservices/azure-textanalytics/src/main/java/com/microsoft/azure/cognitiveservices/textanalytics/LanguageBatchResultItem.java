/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.textanalytics;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The LanguageBatchResultItem model.
 */
public class LanguageBatchResultItem {
    /**
     * Unique document identifier.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /**
     * A list of extracted languages.
     */
    @JsonProperty(value = "detectedLanguages", access = JsonProperty.Access.WRITE_ONLY)
    private List<DetectedLanguage> detectedLanguages;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Get the detectedLanguages value.
     *
     * @return the detectedLanguages value
     */
    public List<DetectedLanguage> detectedLanguages() {
        return this.detectedLanguages;
    }

}
