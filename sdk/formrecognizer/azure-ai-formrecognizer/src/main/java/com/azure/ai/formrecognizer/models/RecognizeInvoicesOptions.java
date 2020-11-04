// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

import static com.azure.ai.formrecognizer.implementation.Utility.DEFAULT_POLL_INTERVAL;

/**
 * Options that may be passed when using recognize invoice APIs on Form Recognizer client.
 */
@Fluent
public final class RecognizeInvoicesOptions {
    private FormContentType contentType;
    private boolean includeFieldElements;
    private String locale;
    private Duration pollInterval = DEFAULT_POLL_INTERVAL;

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
     * @return the updated {@code RecognizeInvoicesOptions} value.
     */
    public RecognizeInvoicesOptions setContentType(final FormContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Set the boolean which specifies if to include form element references in the result.
     *
     * @param includeFieldElements the boolean to specify if to include form element references in the result.
     *
     * @return the updated {@code RecognizeInvoicesOptions} value.
     */
    public RecognizeInvoicesOptions setFieldElementsIncluded(final boolean includeFieldElements) {
        this.includeFieldElements = includeFieldElements;
        return this;
    }

    /**
     * Set the duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @param pollInterval the duration to specify between each poll for the operation status.
     *
     * @return the updated {@code RecognizeInvoicesOptions} value.
     */
    public RecognizeInvoicesOptions setPollInterval(final Duration pollInterval) {
        this.pollInterval = pollInterval == null ? DEFAULT_POLL_INTERVAL : pollInterval;
        return this;
    }

    /**
     * Get the locale information for the invoice.
     * Supported locales include: en-US.
     *
     * @return the localeInfo value.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Set the locale information for the invoice.
     * Supported locales include: en-US.
     *
     * @param locale the localeInfo value.
     * @return the updated {@code RecognizeInvoicesOptions} value.
     */
    public RecognizeInvoicesOptions setLocale(final String locale) {
        this.locale = locale;
        return this;
    }
}
