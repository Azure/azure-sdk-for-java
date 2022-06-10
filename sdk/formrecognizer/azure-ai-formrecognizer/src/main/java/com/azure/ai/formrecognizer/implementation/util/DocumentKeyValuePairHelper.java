// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

/**
 * The helper class to set the non-public properties of an {@link DocumentKeyValuePair} instance.
 */

import com.azure.ai.formrecognizer.models.DocumentKeyValueElement;
import com.azure.ai.formrecognizer.models.DocumentKeyValuePair;
import com.azure.ai.formrecognizer.models.AnalyzedDocument;

/**
 * The helper class to set the non-public properties of an {@link DocumentKeyValuePair} instance.
 */
public final class DocumentKeyValuePairHelper {
    private static DocumentKeyValuePairAccessor accessor;

    private DocumentKeyValuePairHelper() {
    }

    /**
     * The method called from {@link AnalyzedDocument} to set it's accessor.
     *
     * @param documentKeyValueElementAccessor The accessor.
     */
    public static void setAccessor(
        final DocumentKeyValuePairHelper.DocumentKeyValuePairAccessor documentKeyValueElementAccessor) {
        accessor = documentKeyValueElementAccessor;
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentKeyValuePair} instance.
     */
    public interface DocumentKeyValuePairAccessor {
        void setKey(DocumentKeyValuePair documentKeyValuePair, DocumentKeyValueElement key);
        void setValue(DocumentKeyValuePair documentKeyValuePair, DocumentKeyValueElement value);
        void setConfidence(DocumentKeyValuePair documentKeyValuePair, float confidence);
    }

    static void setConfidence(DocumentKeyValuePair documentKeyValuePair, float confidence) {
        accessor.setConfidence(documentKeyValuePair, confidence);
    }

    static void setValue(DocumentKeyValuePair documentKeyValuePair, DocumentKeyValueElement value) {
        accessor.setValue(documentKeyValuePair, value);
    }

    static void setKey(DocumentKeyValuePair documentKeyValuePair, DocumentKeyValueElement key) {
        accessor.setKey(documentKeyValuePair, key);
    }
}
