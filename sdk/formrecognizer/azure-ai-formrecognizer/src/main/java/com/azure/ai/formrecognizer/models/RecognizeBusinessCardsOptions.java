// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

import static com.azure.ai.formrecognizer.implementation.Utility.DEFAULT_POLL_INTERVAL;

/**
 * Options that may be passed when using recognize business card APIs on Form Recognizer client.
 */
@Fluent
public final class RecognizeBusinessCardsOptions {
    private FormContentType contentType;
    private boolean includeFieldElements;
    private Duration pollInterval = DEFAULT_POLL_INTERVAL;
    private FormRecognizerLocale locale;

    /**
     * Get the type of the form. Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return the {@code contentType} value.
     */
    public FormContentType getContentType() {
        return contentType;
    }

    /**
     * Get the boolean which specifies if to include form element references in the result.
     *
     * @return the {@code includeFieldElements} value.
     */
    public boolean isFieldElementsIncluded() {
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
     * @param contentType the provided form content type.
     *
     * @return the updated {@code RecognizeBusinessCardOptions} value.
     */
    public RecognizeBusinessCardsOptions setContentType(final FormContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Set the boolean which specifies if to include form element references in the result.
     *
     * @param includeFieldElements the boolean to specify if to include form element references in the result.
     *
     * @return the updated {@code RecognizeBusinessCardOptions} value.
     */
    public RecognizeBusinessCardsOptions setFieldElementsIncluded(final boolean includeFieldElements) {
        this.includeFieldElements = includeFieldElements;
        return this;
    }

    /**
     * Set the duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @param pollInterval the duration to specify between each poll for the operation status.
     *
     * @return the updated {@code RecognizeBusinessCardOptions} value.
     */
    public RecognizeBusinessCardsOptions setPollInterval(final Duration pollInterval) {
        this.pollInterval = pollInterval == null ? DEFAULT_POLL_INTERVAL : pollInterval;
        return this;
    }

    /**
     * Get the locale value.
     * Supported locales include: en-AU, en-CA, en-GB, en-IN, en-US.
     *
     * @return the locale value.
     */
    public FormRecognizerLocale getLocale() {
        return locale;
    }

    /**
     * Set the locale value.
     * Supported locales include: en-AU, en-CA, en-GB, en-IN, en-US.
     *
     * @param locale the locale value to set.
     *
     * @return the locale value.
     */
    public RecognizeBusinessCardsOptions setLocale(final FormRecognizerLocale locale) {
        this.locale = locale;
        return this;
    }
}
