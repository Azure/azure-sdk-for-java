// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.BoundingRegion;
import com.azure.ai.formrecognizer.models.DocumentKeyValueElement;
import com.azure.ai.formrecognizer.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.models.DocumentSpan;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentKeyValueElement} instance.
 */

/**
 * The helper class to set the non-public properties of an {@link DocumentKeyValueElement} instance.
 */
public final class DocumentKeyValueElementHelper {
    private static DocumentKeyValueElementAccessor accessor;

    private DocumentKeyValueElementHelper() {
    }

    /**
     * The method called from {@link AnalyzedDocument} to set it's accessor.
     *
     * @param documentKeyValueElementAccessor The accessor.
     */
    public static void setAccessor(
        final DocumentKeyValueElementHelper.DocumentKeyValueElementAccessor documentKeyValueElementAccessor) {
        accessor = documentKeyValueElementAccessor;
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentKeyValueElement} instance.
     */
    public interface DocumentKeyValueElementAccessor {
        void setContent(DocumentKeyValueElement documentKeyValueElement, String content);
        void setBoundingRegions(DocumentKeyValueElement documentKeyValueElement, List<BoundingRegion> boundingRegions);
        void setSpans(DocumentKeyValueElement documentKeyValueElement, List<DocumentSpan> spans);

    }

    static void setContent(DocumentKeyValueElement documentKeyValueElement, String content) {
        accessor.setContent(documentKeyValueElement, content);
    }

    static void setBoundingRegions(DocumentKeyValueElement documentKeyValueElement, List<BoundingRegion> boundingRegions) {
        accessor.setBoundingRegions(documentKeyValueElement, boundingRegions);
    }

    static void setSpans(DocumentKeyValueElement documentKeyValueElement, List<DocumentSpan> spans) {
        accessor.setSpans(documentKeyValueElement, spans);
    }
}
