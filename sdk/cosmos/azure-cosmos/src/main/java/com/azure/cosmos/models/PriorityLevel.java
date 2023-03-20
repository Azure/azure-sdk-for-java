// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;

/**
 * Enumeration specifying Priority Level of requests
 */
@Beta(value = Beta.SinceVersion.V4_41_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public enum PriorityLevel {
    /**
     * High Priority level
     */
    High(1),
    /**
     * Low Priority level
     */
    Low(2);

    private final int priorityValue;

    PriorityLevel(int priorityValue) {
        this.priorityValue = priorityValue;
    }

    /**
     * Gets the numerical value of priority level
     * @return priority numerical value.
     */
    public int getPriorityValue() {
        return this.priorityValue;
    }
}
