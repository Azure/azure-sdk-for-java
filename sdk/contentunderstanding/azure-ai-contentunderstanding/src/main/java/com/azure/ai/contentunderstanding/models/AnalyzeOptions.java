// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.models;

import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * Options for analysis operations that take {@link AnalysisInput} values.
 */
@Fluent
public final class AnalyzeOptions {
    private Map<String, String> modelDeployments;
    private Boolean allowInputTruncation;
    private ProcessingLocation processingLocation;

    /**
     * Creates an instance of {@link AnalyzeOptions}.
     */
    public AnalyzeOptions() {
    }

    /**
     * Gets the override mapping of model names to deployments.
     *
     * @return The model deployment mappings, or {@code null} to use service defaults.
     */
    public Map<String, String> getModelDeployments() {
        return modelDeployments;
    }

    /**
     * Sets the override mapping of model names to deployments.
     *
     * @param modelDeployments The model deployment mappings, or {@code null} to use service defaults.
     * @return The updated options.
     */
    public AnalyzeOptions setModelDeployments(Map<String, String> modelDeployments) {
        this.modelDeployments = modelDeployments;
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
    public AnalyzeOptions setInputTruncationAllowed(Boolean allowInputTruncation) {
        this.allowInputTruncation = allowInputTruncation;
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
    public AnalyzeOptions setProcessingLocation(ProcessingLocation processingLocation) {
        this.processingLocation = processingLocation;
        return this;
    }
}
