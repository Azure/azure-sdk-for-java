// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.models.DocumentAnnotation;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentAnnotationKind;
import com.azure.ai.formrecognizer.documentanalysis.models.Point;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentAnnotation} instance.
 */
public final class DocumentAnnotationHelper {
    private static DocumentAnnotationAccessor accessor;

    private DocumentAnnotationHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentAnnotation} instance.
     */
    public interface DocumentAnnotationAccessor {
        void setPolygon(DocumentAnnotation documentAnnotation, List<Point> polygon);

        void setKind(DocumentAnnotation documentAnnotation, DocumentAnnotationKind kind);



        void setConfidence(DocumentAnnotation documentAnnotation, float confidence);
    }

    /**
     * The method called from {@link DocumentAnnotation} to set it's accessor.
     *
     * @param documentAnnotationAccessor The accessor.
     */
    public static void setAccessor(final DocumentAnnotationHelper.DocumentAnnotationAccessor documentAnnotationAccessor) {
        accessor = documentAnnotationAccessor;
    }

    static void setPolygon(DocumentAnnotation documentAnnotation, List<Point> points) {
        accessor.setPolygon(documentAnnotation, points);
    }

    static void setKind(DocumentAnnotation documentAnnotation, DocumentAnnotationKind kind) {
        accessor.setKind(documentAnnotation, kind);
    }

    static void setConfidence(DocumentAnnotation documentAnnotation, float confidence) {
        accessor.setConfidence(documentAnnotation, confidence);
    }
}
