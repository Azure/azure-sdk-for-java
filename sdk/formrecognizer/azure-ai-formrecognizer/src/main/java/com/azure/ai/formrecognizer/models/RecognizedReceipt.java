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
     * The recognized form.
     */
    private final RecognizedForm recognizedForm;

    /**
     * Constructs a RecognizedReceipt object.
     *
     * @param recognizedForm The recognized form.
     */
    public RecognizedReceipt(final RecognizedForm recognizedForm) {
        this.recognizedForm = recognizedForm;
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
