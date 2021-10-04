// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.BoundingRegion;
import com.azure.ai.formrecognizer.models.DocumentFieldType;
import com.azure.ai.formrecognizer.models.DocumentSignatureType;
import com.azure.ai.formrecognizer.models.SelectionMarkState;
import com.azure.ai.formrecognizer.models.DocumentField;
import com.azure.ai.formrecognizer.models.DocumentSpan;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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

        void setValueString(DocumentField documentField, String valueString);

        void setValueDate(DocumentField documentField, LocalDate valueDate);

        void setValueTime(DocumentField documentField, LocalTime valueTime);

        void setValuePhoneNumber(DocumentField documentField, String valuePhoneNumber);

        void setValueNumber(DocumentField documentField, Float valueNumber);

        void setValueInteger(DocumentField documentField, Long valueInteger);

        void setValueSelectionMark(DocumentField documentField, SelectionMarkState valueSelectionMark);

        void setValueSignature(DocumentField documentField, DocumentSignatureType valueSignature);

        void setValueCountryRegion(DocumentField documentField, String valueCountryRegion);

        void setValueArray(DocumentField documentField, List<DocumentField> valueArray);

        void setValueObject(DocumentField documentField, Map<String, DocumentField> valueObject);

        void setContent(DocumentField documentField, String content);

        void setBoundingRegions(DocumentField documentField, List<BoundingRegion> boundingRegions);

        void setSpans(DocumentField documentField, List<DocumentSpan> spans);

        void setConfidence(DocumentField documentField, Float confidence);
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

    static void setValueString(DocumentField documentField, String valueString) {
        accessor.setValueString(documentField, valueString);
    }

    static void setValueDate(DocumentField documentField, LocalDate valueDate) {
        accessor.setValueDate(documentField, valueDate);
    }

    static void setValueTime(DocumentField documentField, LocalTime valueTime) {
        accessor.setValueTime(documentField, valueTime);
    }

    static void setValuePhoneNumber(DocumentField documentField, String valuePhoneNumber) {
        accessor.setValuePhoneNumber(documentField, valuePhoneNumber);
    }

    static void setValueNumber(DocumentField documentField, Float valueNumber) {
        accessor.setValueNumber(documentField, valueNumber);
    }

    static void setValueInteger(DocumentField documentField, Long valueInteger) {
        accessor.setValueInteger(documentField, valueInteger);
    }

    static void setValueSelectionMark(DocumentField documentField, SelectionMarkState valueSelectionMark) {
        accessor.setValueSelectionMark(documentField, valueSelectionMark);
    }

    static void setValueSignature(DocumentField documentField, DocumentSignatureType valueSignature) {
        accessor.setValueSignature(documentField, valueSignature);
    }

    static void setValueCountryRegion(DocumentField documentField, String valueCountryRegion) {
        accessor.setValueCountryRegion(documentField, valueCountryRegion);
    }

    static void setValueArray(DocumentField documentField, List<DocumentField> valueArray) {
        accessor.setValueArray(documentField, valueArray);
    }

    static void setValueObject(DocumentField documentField, Map<String, DocumentField> valueObject) {
        accessor.setValueObject(documentField, valueObject);
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
}
