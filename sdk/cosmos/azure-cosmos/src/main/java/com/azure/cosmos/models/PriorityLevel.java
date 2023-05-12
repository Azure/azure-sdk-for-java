// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Enumeration specifying Priority Level of requests
 */
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
}
