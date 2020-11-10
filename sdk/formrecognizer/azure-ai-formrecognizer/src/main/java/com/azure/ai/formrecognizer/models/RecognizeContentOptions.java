// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

import static com.azure.ai.formrecognizer.implementation.Utility.DEFAULT_POLL_INTERVAL;

/**
 * Options that may be passed when using recognize content APIs on Form Recognizer client.
 */
@Fluent
public final class RecognizeContentOptions {
    private FormContentType contentType;
    private Duration pollInterval = DEFAULT_POLL_INTERVAL;
    private String language;

    /**
     * Get the type of the form. Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return the {@code contentType} value.
     */
    public FormContentType getContentType() {
        return contentType;
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
     * @return the updated {@code RecognizeContentOptions} value.
     */
    public RecognizeContentOptions setContentType(final FormContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Set the duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @param pollInterval the duration to specify between each poll for the operation status.
     *
     * @return the updated {@code RecognizeContentOptions} value.
     */
    public RecognizeContentOptions setPollInterval(final Duration pollInterval) {
        this.pollInterval = pollInterval == null ? DEFAULT_POLL_INTERVAL : pollInterval;
        return this;
    }

    /**
     * Get the BCP-47 language code of the text in the document.
     * See supported language codes here:
     * <a>
     * https://docs.microsoft.com/azure/cognitive-services/form-recognizer/language-support.
     * </a>
     *
     * @return the language code for the text in the document.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Set the BCP-47 language code of the text in the document.
     * See supported language codes here:
     * <a>
     * https://docs.microsoft.com/azure/cognitive-services/form-recognizer/language-support.
     * </a>
     *
     * @param language the language code value to set.
     * @return the updated {@code RecognizeContentOptions} value.
     */
    public RecognizeContentOptions setLanguage(String language) {
        this.language = language;
        return this;
    }
}
