/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The list of job ids.
 */
public class JobListResult {
    /**
     * The job id.
     */
    @JsonProperty(value = "Value")
    private List<String> value;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<String> value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the JobListResult object itself.
     */
    public JobListResult withValue(List<String> value) {
        this.value = value;
        return this;
    }

}
