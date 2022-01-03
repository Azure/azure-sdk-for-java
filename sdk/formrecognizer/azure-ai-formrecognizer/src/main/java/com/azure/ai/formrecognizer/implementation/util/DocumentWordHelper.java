// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.DocumentWord;
import com.azure.ai.formrecognizer.models.DocumentSpan;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentWord} instance.
 */
public final class DocumentWordHelper {
    private static DocumentWordAccessor accessor;

    private DocumentWordHelper() {
    }

    /**
     * The method called from {@link DocumentWord} to set it's accessor.
     *
     * @param documentLineAccessor The accessor.
     */
    public static void setAccessor(final DocumentWordHelper.DocumentWordAccessor documentLineAccessor) {
        accessor = documentLineAccessor;
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentWord} instance.
     */
    public interface DocumentWordAccessor {
        void setBoundingBox(DocumentWord documentWord, List<Float> boundingBox);
        void setContent(DocumentWord documentWord, String content);
        void setSpan(DocumentWord documentWord, DocumentSpan span);
        void setConfidence(DocumentWord documentWord, float confidence);

    }

    static void setBoundingBox(DocumentWord documentWord, List<Float> boundingBox) {
        accessor.setBoundingBox(documentWord, boundingBox);
    }

    static void setContent(DocumentWord documentWord, String content) {
        accessor.setContent(documentWord, content);
    }

    static void setSpan(DocumentWord documentWord, DocumentSpan span) {
        accessor.setSpan(documentWord, span);
    }

    static void setConfidence(DocumentWord documentWord, float confidence) {
        accessor.setConfidence(documentWord, confidence);
    }
}
