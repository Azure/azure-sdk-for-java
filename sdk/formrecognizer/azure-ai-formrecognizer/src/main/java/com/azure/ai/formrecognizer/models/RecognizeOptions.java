// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;

/**
 * Options that may be passed when using recognize APIs on Form Recognizer client.
 */
@Fluent
public class RecognizeOptions {
    private final InputStream form;
    private final Flux<ByteBuffer> formData;
    private final long length;
    private final String formUrl;
    private FormContentType formContentType;
    private boolean includeFieldElements;
    private Duration pollInterval = DEFAULT_POLL_INTERVAL;
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);

    /**
     * Create a {@code RecognizeOptions option} object.
     *
     * @param form The {@code InputStream data} of the form to recognize form information from.
     * @param length the exact length of the provided form data.
     */
    public RecognizeOptions(final InputStream form, final long length) {
        this.form = form;
        this.length = length;
        this.formData = null;
        this.formUrl = null;
    }

    /**
     * Create a {@code RecognizeOptions option} object.
     *
     * @param formData The {@code ByteBuffer data} of the form to recognize form information from.
     * @param length the exact length of the provided form data.
     */
    public RecognizeOptions(final Flux<ByteBuffer> formData, final long length) {
        this.formData = formData;
        this.length = length;
        this.form = null;
        this.formUrl = null;
    }

    /**
     * Create a {@code RecognizeOptions option} object.
     *
     * @param formUrl The source URL to the input form.
     */
    public RecognizeOptions(final String formUrl) {
        this.formUrl = formUrl;
        this.form = null;
        this.formData = null;
        this.length = 0;
    }

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
     * Get the {@code InputStream data} of the form to recognize form information from.
     *
     * @return the {@code form} value.
     */
    public InputStream getForm() {
        return form;
    }

    /**
     * Get the the source URL to the input form.
     *
     * @return the {@code formUrl} value.
     */
    public String getFormUrl() {
        return formUrl;
    }

    /**
     * Get the exact length of the provided form data. Size of the file must be less than 50 MB.
     *
     * @return the {@code length} value.
     */
    public long getLength() {
        return length;
    }

    /**
     * Get the {@code ByteBuffer data} of the form to recognize form information from.
     *
     * @return the {@code formData} value.
     */
    public Flux<ByteBuffer> getFormData() {
        return formData;
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
