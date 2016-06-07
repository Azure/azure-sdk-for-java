/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Data Lake Analytics job error details.
 */
public class JobErrorDetails {
    /**
     * Gets the error message description.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String description;

    /**
     * Gets the details of the error message.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String details;

    /**
     * Gets the end offset in the job where the error was found.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer endOffset;

    /**
     * Gets the specific identifier for the type of error encountered in the
     * job.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String errorId;

    /**
     * Gets the path to any supplemental error files, if any.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String filePath;

    /**
     * Gets the link to MSDN or Azure help for this type of error, if any.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String helpLink;

    /**
     * Gets the internal diagnostic stack trace if the user requesting the job
     * error details has sufficient permissions it will be retrieved,
     * otherwise it will be empty.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String internalDiagnostics;

    /**
     * Gets the specific line number in the job where the error occured.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer lineNumber;

    /**
     * Gets the user friendly error message for the failure.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String message;

    /**
     * Gets the recommended resolution for the failure, if any.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String resolution;

    /**
     * Gets the inner error of this specific job error message, if any.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private JobInnerError innerError;

    /**
     * Gets the severity level of the failure. Possible values include:
     * 'Warning', 'Error', 'Info'.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private SeverityTypes severity;

    /**
     * Gets the ultimate source of the failure (usually either SYSTEM or USER).
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String source;

    /**
     * Gets the start offset in the job where the error was found.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer startOffset;

    /**
     * Get the description value.
     *
     * @return the description value
     */
    public String description() {
        return this.description;
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
     * Get the endOffset value.
     *
     * @return the endOffset value
     */
    public Integer endOffset() {
        return this.endOffset;
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
     * Get the filePath value.
     *
     * @return the filePath value
     */
    public String filePath() {
        return this.filePath;
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
     * Get the lineNumber value.
     *
     * @return the lineNumber value
     */
    public Integer lineNumber() {
        return this.lineNumber;
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
     * Get the innerError value.
     *
     * @return the innerError value
     */
    public JobInnerError innerError() {
        return this.innerError;
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
     * Get the source value.
     *
     * @return the source value
     */
    public String source() {
        return this.source;
    }

    /**
     * Get the startOffset value.
     *
     * @return the startOffset value
     */
    public Integer startOffset() {
        return this.startOffset;
    }

}
