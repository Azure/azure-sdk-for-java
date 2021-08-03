// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;
import java.util.List;

import static com.azure.ai.formrecognizer.implementation.Utility.DEFAULT_POLL_INTERVAL;

/**
 * Options that may be passed when using recognize content APIs on Form Recognizer client.
 */
@Fluent
public final class RecognizeContentOptions {
    private FormContentType contentType;
    private Duration pollInterval = DEFAULT_POLL_INTERVAL;
    private FormRecognizerLanguage language;
    private List<String> pages;
    private FormReadingOrder readingOrder;

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
     * See supported language codes
     * <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/language-support?tabs=v2-1">here</a>.
     *
     * @return the language code for the text in the document.
     */
    public FormRecognizerLanguage getLanguage() {
        return language;
    }

    /**
     * Set the BCP-47 language code of the text in the document.
     * See supported language codes
     * <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/language-support?tabs=v2-1">here</a>.
     *
     * @param language the language code value to set.
     * @return the updated {@code RecognizeContentOptions} value.
     */
    public RecognizeContentOptions setLanguage(FormRecognizerLanguage language) {
        this.language = language;
        return this;
    }

    /**
     * Get the custom page numbers for multi-page documents(PDF/TIFF). Input the number of the
     * pages you want to get the recognized result for.
     * <p>For a range of pages, use a hyphen, ex - ["1-3"]. Separate each page or a page
     * range with a comma, ex - ["1-3", 4].</p>
     *
     * @return the list of custom page numbers for a multi page document.
     */
    public List<String> getPages() {
        return pages;
    }

    /**
     * Set the custom page numbers for multi-page documents(PDF/TIFF). Input the number of the
     * pages you want to get the recognized result for.
     * <p>For a range of pages, use a hyphen, ex - ["1-3"]. Separate each page or a page
     * range with a comma, ex - ["1-3", 4].</p>
     *
     * @param pages the custom page numbers value to set.
     * @return the updated {@code RecognizeContentOptions} value.
     */
    public RecognizeContentOptions setPages(List<String> pages) {
        this.pages = pages;
        return this;
    }

    /**
     * Get the order in which recognized text lines are returned.
     *
     * @return the order in which the recognized lines are returned.
     */
    public FormReadingOrder getReadingOrder() {
        return readingOrder;
    }

    /**
     * Specifies the order in which recognized text lines are returned. As the sorting order
     * depends on the detected text, it may change across images and OCR version updates. Thus,
     * business logic should be built upon the actual line location instead of order.
     *
     * @param readingOrder the order specifies in which text lines are returned
     * @return the updated {@code RecognizeContentOptions} value.
     */
    public RecognizeContentOptions setReadingOrder(FormReadingOrder readingOrder) {
        this.readingOrder = readingOrder;
        return this;
    }
}
