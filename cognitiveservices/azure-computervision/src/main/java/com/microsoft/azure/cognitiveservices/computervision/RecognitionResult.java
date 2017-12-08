/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The RecognitionResult model.
 */
public class RecognitionResult {
    /**
     * The lines property.
     */
    @JsonProperty(value = "lines")
    private List<Line> lines;

    /**
     * Get the lines value.
     *
     * @return the lines value
     */
    public List<Line> lines() {
        return this.lines;
    }

    /**
     * Set the lines value.
     *
     * @param lines the lines value to set
     * @return the RecognitionResult object itself.
     */
    public RecognitionResult withLines(List<Line> lines) {
        this.lines = lines;
        return this;
    }

}
