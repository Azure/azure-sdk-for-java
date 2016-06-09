/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Data Lake Analytics job error details.
 */
public class JobInnerError {
    /**
     * the diagnostic error code.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer diagnosticCode;

    /**
     * the severity level of the failure. Possible values include: 'Warning',
     * 'Error', 'Info'.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private SeverityTypes severity;

    /**
     * the details of the error message.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String details;

    /**
     * the component that failed.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String component;

    /**
     * the specific identifier for the type of error encountered in the job.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String errorId;

    /**
     * the link to MSDN or Azure help for this type of error, if any.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String helpLink;

    /**
     * the internal diagnostic stack trace if the user requesting the job
     * error details has sufficient permissions it will be retrieved,
     * otherwise it will be empty.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String internalDiagnostics;

    /**
     * the user friendly error message for the failure.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String message;

    /**
     * the recommended resolution for the failure, if any.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String resolution;

    /**
     * the ultimate source of the failure (usually either SYSTEM or USER).
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String source;

    /**
     * the error message description.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String description;

    /**
     * Get the diagnosticCode value.
     *
     * @return the diagnosticCode value
     */
    public Integer diagnosticCode() {
        return this.diagnosticCode;
    }

    /**
     * Get the severity value.
     *
     * @return the severity value
     */
    public SeverityTypes severity() {
        return this.severity;
    }

    /**
     * Get the details value.
     *
     * @return the details value
     */
    public String details() {
        return this.details;
    }

    /**
     * Get the component value.
     *
     * @return the component value
     */
    public String component() {
        return this.component;
    }

    /**
     * Get the errorId value.
     *
     * @return the errorId value
     */
    public String errorId() {
        return this.errorId;
    }

    /**
     * Get the helpLink value.
     *
     * @return the helpLink value
     */
    public String helpLink() {
        return this.helpLink;
    }

    /**
     * Get the internalDiagnostics value.
     *
     * @return the internalDiagnostics value
     */
    public String internalDiagnostics() {
        return this.internalDiagnostics;
    }

    /**
     * Get the message value.
     *
     * @return the message value
     */
    public String message() {
        return this.message;
    }

    /**
     * Get the resolution value.
     *
     * @return the resolution value
     */
    public String resolution() {
        return this.resolution;
    }

    /**
     * Get the source value.
     *
     * @return the source value
     */
    public String source() {
        return this.source;
    }

    /**
     * Get the description value.
     *
     * @return the description value
     */
    public String description() {
        return this.description;
    }

}
