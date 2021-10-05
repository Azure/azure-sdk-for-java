// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.DocumentLine;
import com.azure.ai.formrecognizer.models.DocumentSpan;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentLine} instance.
 */
public final class DocumentLineHelper {
    private static DocumentLineAccessor accessor;

    private DocumentLineHelper() {
    }

    /**
     * The method called from {@link DocumentLine} to set it's accessor.
     *
     * @param documentLineAccessor The accessor.
     */
    public static void setAccessor(final DocumentLineHelper.DocumentLineAccessor documentLineAccessor) {
        accessor = documentLineAccessor;
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentLine} instance.
     */
    public interface DocumentLineAccessor {
        void setContent(DocumentLine documentLine, String content);
        void setBoundingBox(DocumentLine documentLine, List<Float> boundingBox);
        void setSpans(DocumentLine documentLine, List<DocumentSpan> spans);
    }

    static void setContent(DocumentLine documentLine, String content) {
        accessor.setContent(documentLine, content);
    }

    static void setBoundingBox(DocumentLine documentLine, List<Float> boundingBox) {
        accessor.setBoundingBox(documentLine, boundingBox);
    }

    static void setSpans(DocumentLine documentLine, List<DocumentSpan> spans) {
        accessor.setSpans(documentLine, spans);
    }
}
