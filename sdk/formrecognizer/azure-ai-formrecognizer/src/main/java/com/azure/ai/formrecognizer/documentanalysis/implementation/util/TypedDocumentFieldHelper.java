// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.models.BoundingRegion;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentField;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFieldType;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSpan;
import com.azure.ai.formrecognizer.documentanalysis.models.TypedDocumentField;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link TypedDocumentField} instance.
 */
public final class TypedDocumentFieldHelper {
    private static TypedDocumentFieldHelper.TypedDocumentFieldAccessor accessor;

    private TypedDocumentFieldHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link TypedDocumentField} instance.
     */
    public interface TypedDocumentFieldAccessor {
        <T> void setValue(TypedDocumentField<T> typedDocumentField, T value);

        <T> void setType(TypedDocumentField<T> typedDocumentField, DocumentFieldType type);

        <T> void setContent(TypedDocumentField<T> typedDocumentField, String content);

        <T> void setBoundingRegions(TypedDocumentField<T> typedDocumentField, List<BoundingRegion> boundingRegions);

        <T> void setSpans(TypedDocumentField<T> typedDocumentField, List<DocumentSpan> spans);

        <T> void setConfidence(TypedDocumentField<T> typedDocumentField, Float confidence);
    }

    /**
     * The method called from {@link DocumentField} to set it's accessor.
     *
     * @param typedDocumentFieldAccessor The accessor.
     */
    public static void setAccessor(final TypedDocumentFieldHelper.TypedDocumentFieldAccessor typedDocumentFieldAccessor) {
        accessor = typedDocumentFieldAccessor;
    }

    static <T> void setValue(TypedDocumentField<T> typedDocumentField, T value) {
        accessor.setValue(typedDocumentField, value);
    }

    static <T> void setType(TypedDocumentField<T> typedDocumentField, DocumentFieldType type) {
        accessor.setType(typedDocumentField, type);
    }

    static <T> void setContent(TypedDocumentField<T> typedDocumentField, String content) {
        accessor.setContent(typedDocumentField, content);
    }

    static <T> void setBoundingRegions(TypedDocumentField<T> typedDocumentField, List<BoundingRegion> boundingRegions) {
        accessor.setBoundingRegions(typedDocumentField, boundingRegions);
    }

    static <T> void setSpans(TypedDocumentField<T> typedDocumentField, List<DocumentSpan> spans) {
        accessor.setSpans(typedDocumentField, spans);
    }

    static <T> void setConfidence(TypedDocumentField<T> typedDocumentField, Float confidence) {
        accessor.setConfidence(typedDocumentField, confidence);
    }
}
