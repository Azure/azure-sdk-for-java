// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.models.DocumentImage;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSpan;
import com.azure.ai.formrecognizer.documentanalysis.models.Point;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentImage} instance.
 */
public final class DocumentImageHelper {
    private static DocumentImageAccessor accessor;

    private DocumentImageHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentImage} instance.
     */
    public interface DocumentImageAccessor {
        void setSpan(DocumentImage documentImage, DocumentSpan span);
        void setConfidence(DocumentImage documentImage, float confidence);
        void setBoundingPolygon(DocumentImage documentImage, List<Point> polygon);
        void setPageNumber(DocumentImage documentImage, int pageNumber);
    }

    /**
     * The method called from {@link DocumentImage} to set it's accessor.
     *
     * @param documentImageAccessor The accessor.
     */
    public static void setAccessor(final DocumentImageAccessor documentImageAccessor) {
        accessor = documentImageAccessor;
    }

    static void setSpan(DocumentImage documentImage, DocumentSpan span) {
        accessor.setSpan(documentImage, span);
    }

    static void setConfidence(DocumentImage documentImage, float confidence) {
        accessor.setConfidence(documentImage, confidence);
    }

    static void setBoundingPolygon(DocumentImage documentImage, List<Point> polygon) {
        accessor.setBoundingPolygon(documentImage, polygon);
    }

    static void setPageNumber(DocumentImage documentImage, int pageNumber) {
        accessor.setPageNumber(documentImage, pageNumber);
    }
}
