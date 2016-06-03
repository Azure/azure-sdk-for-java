/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;


/**
 * The HiveJobStatementInfo model.
 */
public class HiveJobStatementInfo {
    /**
     * Gets or sets the log location for this statement.
     */
    private String logLocation;

    /**
     * Gets or sets the result preview location for this statement.
     */
    private String resultPreviewLocation;

    /**
     * Gets or sets the result location for this statement.
     */
    private String resultLocation;

    /**
     * Gets or sets the error message for this statement.
     */
    private String errorMessage;

    /**
     * Get the logLocation value.
     *
     * @return the logLocation value
     */
    public String logLocation() {
        return this.logLocation;
    }

    /**
     * Set the logLocation value.
     *
     * @param logLocation the logLocation value to set
     * @return the HiveJobStatementInfo object itself.
     */
    public HiveJobStatementInfo withLogLocation(String logLocation) {
        this.logLocation = logLocation;
        return this;
    }

    /**
     * Get the resultPreviewLocation value.
     *
     * @return the resultPreviewLocation value
     */
    public String resultPreviewLocation() {
        return this.resultPreviewLocation;
    }

    /**
     * Set the resultPreviewLocation value.
     *
     * @param resultPreviewLocation the resultPreviewLocation value to set
     * @return the HiveJobStatementInfo object itself.
     */
    public HiveJobStatementInfo withResultPreviewLocation(String resultPreviewLocation) {
        this.resultPreviewLocation = resultPreviewLocation;
        return this;
    }

    /**
     * Get the resultLocation value.
     *
     * @return the resultLocation value
     */
    public String resultLocation() {
        return this.resultLocation;
    }

    /**
     * Set the resultLocation value.
     *
     * @param resultLocation the resultLocation value to set
     * @return the HiveJobStatementInfo object itself.
     */
    public HiveJobStatementInfo withResultLocation(String resultLocation) {
        this.resultLocation = resultLocation;
        return this;
    }

    /**
     * Get the errorMessage value.
     *
     * @return the errorMessage value
     */
    public String errorMessage() {
        return this.errorMessage;
    }

    /**
     * Set the errorMessage value.
     *
     * @param errorMessage the errorMessage value to set
     * @return the HiveJobStatementInfo object itself.
     */
    public HiveJobStatementInfo withErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

}
