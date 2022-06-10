// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.BoundingRegion;
import com.azure.ai.formrecognizer.models.DocumentEntity;
import com.azure.ai.formrecognizer.models.DocumentSpan;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentEntity} instance.
 */
public final class DocumentEntityHelper {
    private static DocumentEntityAccessor accessor;

    private DocumentEntityHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentEntity} instance.
     */
    public interface DocumentEntityAccessor {
        void setCategory(DocumentEntity documentEntity, String category);
        void setSubCategory(DocumentEntity documentEntity, String subCategory);
        void setContent(DocumentEntity documentEntity, String content);
        void setBoundingRegions(DocumentEntity documentEntity, List<BoundingRegion> boundingRegion);
        void setSpans(DocumentEntity documentEntity, List<DocumentSpan> spans);
        void setConfidence(DocumentEntity documentEntity, Float confidence);
    }

    /**
     * The method called from {@link DocumentEntity} to set it's accessor.
     *
     * @param documentEntityAccessor The accessor.
     */
    public static void setAccessor(final DocumentEntityAccessor documentEntityAccessor) {
        accessor = documentEntityAccessor;
    }

    static void setCategory(DocumentEntity documentEntity, String category) {
        accessor.setCategory(documentEntity, category);
    }

    static void setSubCategory(DocumentEntity documentEntity, String subCategory) {
        accessor.setSubCategory(documentEntity, subCategory);
    }

    static void setContent(DocumentEntity documentEntity, String content) {
        accessor.setContent(documentEntity, content);
    }

    static void setBoundingRegions(DocumentEntity documentEntity, List<BoundingRegion> boundingRegions) {
        accessor.setBoundingRegions(documentEntity, boundingRegions);
    }

    static void setSpans(DocumentEntity documentEntity, List<DocumentSpan> spans) {
        accessor.setSpans(documentEntity, spans);
    }

    static void setConfidence(DocumentEntity documentEntity, Float confidence) {
        accessor.setConfidence(documentEntity, confidence);
    }
}
