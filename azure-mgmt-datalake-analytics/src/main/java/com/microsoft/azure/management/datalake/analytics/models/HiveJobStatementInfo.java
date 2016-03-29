/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;


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
    public String getLogLocation() {
        return this.logLocation;
    }

    /**
     * Set the logLocation value.
     *
     * @param logLocation the logLocation value to set
     */
    public void setLogLocation(String logLocation) {
        this.logLocation = logLocation;
    }

    /**
     * Get the resultPreviewLocation value.
     *
     * @return the resultPreviewLocation value
     */
    public String getResultPreviewLocation() {
        return this.resultPreviewLocation;
    }

    /**
     * Set the resultPreviewLocation value.
     *
     * @param resultPreviewLocation the resultPreviewLocation value to set
     */
    public void setResultPreviewLocation(String resultPreviewLocation) {
        this.resultPreviewLocation = resultPreviewLocation;
    }

    /**
     * Get the resultLocation value.
     *
     * @return the resultLocation value
     */
    public String getResultLocation() {
        return this.resultLocation;
    }

    /**
     * Set the resultLocation value.
     *
     * @param resultLocation the resultLocation value to set
     */
    public void setResultLocation(String resultLocation) {
        this.resultLocation = resultLocation;
    }

    /**
     * Get the errorMessage value.
     *
     * @return the errorMessage value
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Set the errorMessage value.
     *
     * @param errorMessage the errorMessage value to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
