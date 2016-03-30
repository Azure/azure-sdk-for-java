/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for a CloudJobOperations.EvaluateAutoScale request.
 */
public class PoolEvaluateAutoScaleParameter {
    /**
     * Sets a formula for the desired number of compute nodes in the pool.
     */
    @JsonProperty(required = true)
    private String autoScaleFormula;

    /**
     * Get the autoScaleFormula value.
     *
     * @return the autoScaleFormula value
     */
    public String getAutoScaleFormula() {
        return this.autoScaleFormula;
    }

    /**
     * Set the autoScaleFormula value.
     *
     * @param autoScaleFormula the autoScaleFormula value to set
     */
    public void setAutoScaleFormula(String autoScaleFormula) {
        this.autoScaleFormula = autoScaleFormula;
    }

}
