// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.models;

import com.azure.core.annotation.Fluent;

/**
 * Options for binary analysis operations.
 */
@Fluent
public final class AnalyzeBinaryOptions {
    private ContentRange contentRange;
    private Boolean allowInputTruncation;
    private String contentType;
    private ProcessingLocation processingLocation;

    /**
     * Creates an instance of {@link AnalyzeBinaryOptions}.
     */
    public AnalyzeBinaryOptions() {
    }

    /**
     * Gets the range of the input to analyze.
     *
     * @return The content range.
     */
    public ContentRange getContentRange() {
        return contentRange;
    }

    /**
     * Sets the range of the input to analyze.
     *
     * @param contentRange The content range.
     * @return The updated options.
     */
    public AnalyzeBinaryOptions setContentRange(ContentRange contentRange) {
        this.contentRange = contentRange;
        return this;
    }

    /**
     * Gets whether over-limit input may be truncated and returned as a partial result.
     *
     * @return Whether input truncation is allowed, or {@code null} to use the analyzer configuration.
     */
    public Boolean isInputTruncationAllowed() {
        return allowInputTruncation;
    }

    /**
     * Sets whether over-limit input may be truncated and returned as a partial result.
     *
     * @param allowInputTruncation Whether input truncation is allowed, or {@code null} to use the analyzer configuration.
     * @return The updated options.
     */
    public AnalyzeBinaryOptions setInputTruncationAllowed(Boolean allowInputTruncation) {
        this.allowInputTruncation = allowInputTruncation;
        return this;
    }

    /**
     * Gets the request content type.
     *
     * @return The request content type, or {@code null} to use {@code application/octet-stream}.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the request content type.
     *
     * @param contentType The request content type.
     * @return The updated options.
     */
    public AnalyzeBinaryOptions setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Gets the location where the data may be processed.
     *
     * @return The processing location.
     */
    public ProcessingLocation getProcessingLocation() {
        return processingLocation;
    }

    /**
     * Sets the location where the data may be processed.
     *
     * @param processingLocation The processing location.
     * @return The updated options.
     */
    public AnalyzeBinaryOptions setProcessingLocation(ProcessingLocation processingLocation) {
        this.processingLocation = processingLocation;
        return this;
    }
}
