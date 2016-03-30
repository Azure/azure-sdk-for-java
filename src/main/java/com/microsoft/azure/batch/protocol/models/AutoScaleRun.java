/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The results and errors from an execution of a pool autoscale formula.
 */
public class AutoScaleRun {
    /**
     * Gets or sets the time at which the autoscale formula was last evaluated.
     */
    @JsonProperty(required = true)
    private DateTime timestamp;

    /**
     * Gets or sets the final values of all variables used in the evaluation
     * of the autoscale formula.  Each variable value is returned in the form
     * $variable=value, and variables are separated by semicolons.
     */
    private String results;

    /**
     * Gets or sets details of the error encountered evaluating the autoscale
     * formula on the pool, if the evaluation was unsuccessful.
     */
    private AutoScaleRunError error;

    /**
     * Get the timestamp value.
     *
     * @return the timestamp value
     */
    public DateTime getTimestamp() {
        return this.timestamp;
    }

    /**
     * Set the timestamp value.
     *
     * @param timestamp the timestamp value to set
     */
    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get the results value.
     *
     * @return the results value
     */
    public String getResults() {
        return this.results;
    }

    /**
     * Set the results value.
     *
     * @param results the results value to set
     */
    public void setResults(String results) {
        this.results = results;
    }

    /**
     * Get the error value.
     *
     * @return the error value
     */
    public AutoScaleRunError getError() {
        return this.error;
    }

    /**
     * Set the error value.
     *
     * @param error the error value to set
     */
    public void setError(AutoScaleRunError error) {
        this.error = error;
    }

}
