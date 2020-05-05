// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The RecognizedReceipt model.
 */
@Immutable
public class RecognizedReceipt {

    /**
     * The locale information for the recognized Receipt.
     */
    private final String receiptLocale;

    /**
     * The recognized form.
     */
    private final RecognizedForm recognizedForm;

    /**
     * Constructs a RecognizedReceipt object.
     *
     * @param receiptLocale The locale information for the recognized Receipt.
     * @param recognizedForm The recognized form.
     */
    public RecognizedReceipt(final String receiptLocale, final RecognizedForm recognizedForm) {
        this.receiptLocale = receiptLocale;
        this.recognizedForm = recognizedForm;
    }

    /**
     * Get he locale information for the recognized Receipt.
     *
     * @return The locale information for the recognized Receipt.
     */
    public String getReceiptLocale() {
        return this.receiptLocale;
    }

    /**
     * Get the extracted field information form for the provided document.
     *
     * @return The extracted field information form for the provided document.
     */
    public RecognizedForm getRecognizedForm() {
        return this.recognizedForm;
    }
}
