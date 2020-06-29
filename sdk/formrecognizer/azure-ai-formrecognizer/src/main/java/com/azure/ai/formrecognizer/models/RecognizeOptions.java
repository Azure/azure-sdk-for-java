// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

/**
 * Options that may be passed when using recognize APIs on Form Recognizer client.
 */
@Fluent
public class RecognizeOptions {
    private FormContentType formContentType;
    private boolean includeFieldElements;
    private Duration pollInterval = DEFAULT_POLL_INTERVAL;
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);

    /**
     * Get the type of the form. Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return the {@code formContentType} value.
     */
    public FormContentType getFormContentType() {
        return formContentType;
    }

    /**
     * Get the boolean which specifies if to include form element references in the result.
     *
     * @return the {@code includeFieldElements} value.
     */
    public boolean isIncludeFieldElements() {
        return includeFieldElements;
    }

    /**
     * Get the duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return the {@code pollInterval} value.
     */
    public Duration getPollInterval() {
        return pollInterval;
    }

    /**
     * Set the type of the form. Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @param formContentType the provided form content type.
     *
     * @return the updated {@code RecognizeOptions} value.
     */
    public RecognizeOptions setFormContentType(final FormContentType formContentType) {
        this.formContentType = formContentType;
        return this;
    }

    /**
     * Set the boolean which specifies if to include form element references in the result.
     *
     * @param includeFieldElements the boolean to specify if to include form element references in the result.
     *
     * @return the updated {@code RecognizeOptions} value.
     */
    public RecognizeOptions setIncludeFieldElements(final boolean includeFieldElements) {
        this.includeFieldElements = includeFieldElements;
        return this;
    }

    /**
     * Set the duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @param pollInterval the duration to specify between each poll for the operation status.
     *
     * @return the updated {@code RecognizeOptions} value.
     */
    public RecognizeOptions setPollInterval(final Duration pollInterval) {
        this.pollInterval = pollInterval == null ? DEFAULT_POLL_INTERVAL : pollInterval;
        return this;
    }
}
