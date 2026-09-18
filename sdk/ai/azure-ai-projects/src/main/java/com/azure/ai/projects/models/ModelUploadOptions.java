// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.models;

import com.azure.core.annotation.Fluent;
import java.time.Duration;
import java.util.Map;

/** Options for uploading and registering a local model. */
@Fluent
@com.azure.ai.projects.implementation.utils.Beta(warningText = "Preview API. Models=V1Preview")
public final class ModelUploadOptions {
    private FoundryModelWeightType weightType;
    private String baseModel;
    private String description;
    private Map<String, String> tags;
    private String connectionName;
    private FileUploadOptions fileUploadOptions;
    private boolean waitForCompletion = true;
    private Duration timeout = Duration.ofMinutes(5);
    private Duration pollInterval = Duration.ofSeconds(2);

    /** Creates default model upload options. */
    public ModelUploadOptions() {
    }

    /**
     * Gets the model weight type.
     * @return the model weight type.
     */
    public FoundryModelWeightType getWeightType() {
        return weightType;
    }

    /**
    * Sets the model weight type.
    * @param value the model weight type.
     * @return these options.
     */
    public ModelUploadOptions setWeightType(FoundryModelWeightType value) {
        weightType = value;
        return this;
    }

    /**
     * Gets the base model asset ID.
     * @return the base model asset ID.
     */
    public String getBaseModel() {
        return baseModel;
    }

    /**
    * Sets the base model asset ID.
    * @param value the base model asset ID.
     * @return these options.
     */
    public ModelUploadOptions setBaseModel(String value) {
        baseModel = value;
        return this;
    }

    /**
     * Gets the description.
     * @return the description.
     */
    public String getDescription() {
        return description;
    }

    /**
    * Sets the description.
    * @param value the description.
     * @return these options.
     */
    public ModelUploadOptions setDescription(String value) {
        description = value;
        return this;
    }

    /**
     * Gets the tags.
     * @return the tags.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
    * Sets the tags.
    * @param value the tags.
     * @return these options.
     */
    public ModelUploadOptions setTags(Map<String, String> value) {
        tags = value;
        return this;
    }

    /**
     * Gets the storage connection name.
     * @return the storage connection name.
     */
    public String getConnectionName() {
        return connectionName;
    }

    /**
    * Sets the storage connection name.
    * @param value the storage connection name.
     * @return these options.
     */
    public ModelUploadOptions setConnectionName(String value) {
        connectionName = value;
        return this;
    }

    /**
     * Gets the file selection and Blob upload settings.
     * @return the file selection and Blob upload settings.
     */
    public FileUploadOptions getFileUploadOptions() {
        return fileUploadOptions;
    }

    /**
    * Sets the file selection and Blob upload settings.
    * @param value the file selection and Blob upload settings.
     * @return these options.
     */
    public ModelUploadOptions setFileUploadOptions(FileUploadOptions value) {
        fileUploadOptions = value;
        return this;
    }

    /**
     * Gets whether to wait until the model can be retrieved.
     * @return whether to wait until the model can be retrieved.
     */
    public boolean isWaitForCompletion() {
        return waitForCompletion;
    }

    /**
    * Sets whether to wait for registration.
    * @param value whether to wait for registration.
     * @return these options.
     */
    public ModelUploadOptions setWaitForCompletion(boolean value) {
        waitForCompletion = value;
        return this;
    }

    /**
     * Gets the registration timeout.
     * @return the registration timeout (default five minutes).
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
    * Sets the timeout for waiting after registration has been accepted.
    * @param value a positive registration timeout.
     * @return these options.
     * @throws IllegalArgumentException if the duration is null or not positive.
     */
    public ModelUploadOptions setTimeout(Duration value) {
        timeout = positive(value);
        return this;
    }

    /**
     * Gets the polling interval.
     * @return the polling interval (default two seconds).
     */
    public Duration getPollInterval() {
        return pollInterval;
    }

    /**
    * Sets the polling interval.
    * @param value a positive polling interval.
     * @return these options.
     * @throws IllegalArgumentException if the duration is null or not positive.
     */
    public ModelUploadOptions setPollInterval(Duration value) {
        pollInterval = positive(value);
        return this;
    }

    private static Duration positive(Duration value) {
        if (value == null || value.isNegative() || value.isZero()) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        return value;
    }
}
