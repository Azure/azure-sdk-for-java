/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;


/**
 * Compute-specific operation properties, including output.
 */
public class ComputeLongRunningOperationProperties {
    /**
     * Operation output data (raw JSON).
     */
    private Object output;

    /**
     * Get the output value.
     *
     * @return the output value
     */
    public Object output() {
        return this.output;
    }

    /**
     * Set the output value.
     *
     * @param output the output value to set
     * @return the ComputeLongRunningOperationProperties object itself.
     */
    public ComputeLongRunningOperationProperties withOutput(Object output) {
        this.output = output;
        return this;
    }

}
