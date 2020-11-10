// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

/**
 * Describes the direction of boundary used in anomaly boundary conditions.
 */
public enum BoundaryDirection {
    /**
     * Defines the lower boundary in a boundary condition.
     */
    LOWER,
    /**
     * Defines the upper boundary in a boundary condition.
     */
    UPPER,
    /**
     * Defines both lower and upper boundary in a boundary condition.
     */
    BOTH
}
