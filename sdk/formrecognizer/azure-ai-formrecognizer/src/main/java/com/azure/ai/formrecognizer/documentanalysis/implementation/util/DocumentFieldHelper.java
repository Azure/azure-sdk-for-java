// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.models.BoundingRegion;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentField;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFieldType;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSpan;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentField} instance.
 */
public final class DocumentFieldHelper {
    private static DocumentFieldHelper.DocumentFieldAccessor accessor;

    private DocumentFieldHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentField} instance.
     */
    public interface DocumentFieldAccessor {
        void setType(DocumentField documentField, DocumentFieldType type);

        void setContent(DocumentField documentField, String content);

        void setBoundingRegions(DocumentField documentField, List<BoundingRegion> boundingRegions);

        void setSpans(DocumentField documentField, List<DocumentSpan> spans);

        void setConfidence(DocumentField documentField, Float confidence);

        void setValue(DocumentField documentField, Object value);

    }

    /**
     * The method called from {@link DocumentField} to set it's accessor.
     *
     * @param documentFieldAccessor The accessor.
     */
    public static void setAccessor(final DocumentFieldHelper.DocumentFieldAccessor documentFieldAccessor) {
        accessor = documentFieldAccessor;
    }

    static void setType(DocumentField documentField, DocumentFieldType type) {
        accessor.setType(documentField, type);
    }

    static void setContent(DocumentField documentField, String content) {
        accessor.setContent(documentField, content);
    }

    static void setBoundingRegions(DocumentField documentField, List<BoundingRegion> boundingRegions) {
        accessor.setBoundingRegions(documentField, boundingRegions);
    }

    static void setSpans(DocumentField documentField, List<DocumentSpan> spans) {
        accessor.setSpans(documentField, spans);
    }

    static void setConfidence(DocumentField documentField, Float confidence) {
        accessor.setConfidence(documentField, confidence);
    }

    static void setValue(DocumentField documentField, Object value) {
        accessor.setValue(documentField, value);
    }
}
