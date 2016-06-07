/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A collection of Azure Batch tasks to add.
 */
public class TaskAddCollectionParameter {
    /**
     * The collection of tasks to add.
     */
    @JsonProperty(required = true)
    private List<TaskAddParameter> value;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<TaskAddParameter> value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the TaskAddCollectionParameter object itself.
     */
    public TaskAddCollectionParameter withValue(List<TaskAddParameter> value) {
        this.value = value;
        return this;
    }

}
