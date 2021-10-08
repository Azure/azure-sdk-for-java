// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentKeyValuePairHelper;

/**
 * An object representing a form field with distinct field label (key) and field value (may be empty).
 */
public final class DocumentKeyValuePair {
    /*
     * Field label of the key-value pair.
     */
    private DocumentKeyValueElement key;

    /*
     * Field value of the key-value pair.
     */
    private DocumentKeyValueElement value;

    /*
     * Confidence of correctly extracting the key-value pair.
     */
    private float confidence;

    /**
     * Get the key property: Field label of the key-value pair.
     *
     * @return the key value.
     */
    public DocumentKeyValueElement getKey() {
        return this.key;
    }

    /**
     * Set the key property: Field label of the key-value pair.
     *
     * @param key the key value to set.
     * @return the DocumentKeyValuePair object itself.
     */
    void setKey(DocumentKeyValueElement key) {
        this.key = key;
    }

    /**
     * Get the value property: Field value of the key-value pair.
     *
     * @return the value value.
     */
    public DocumentKeyValueElement getValue() {
        return this.value;
    }

    /**
     * Set the value property: Field value of the key-value pair.
     *
     * @param value the value value to set.
     * @return the DocumentKeyValuePair object itself.
     */
    void setValue(DocumentKeyValueElement value) {
        this.value = value;
    }

    /**
     * Get the confidence property: Confidence of correctly extracting the key-value pair.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    /**
     * Set the confidence property: Confidence of correctly extracting the key-value pair.
     *
     * @param confidence the confidence value to set.
     * @return the DocumentKeyValuePair object itself.
     */
    void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    static {
        DocumentKeyValuePairHelper.setAccessor(new DocumentKeyValuePairHelper.DocumentKeyValuePairAccessor() {
            @Override
            public void setKey(DocumentKeyValuePair documentKeyValuePair, DocumentKeyValueElement key) {
                documentKeyValuePair.setKey(key);
            }

            @Override
            public void setValue(DocumentKeyValuePair documentKeyValuePair, DocumentKeyValueElement value) {
                documentKeyValuePair.setValue(value);
            }

            @Override
            public void setConfidence(DocumentKeyValuePair documentKeyValuePair, float confidence) {
                documentKeyValuePair.setConfidence(confidence);
            }
        });
    }
}
