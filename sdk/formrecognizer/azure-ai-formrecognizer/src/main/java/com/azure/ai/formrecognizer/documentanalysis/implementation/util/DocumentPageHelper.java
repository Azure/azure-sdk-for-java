// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.models.DocumentBarcode;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentAnnotation;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFormula;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentImage;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentLine;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentPage;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentPageKind;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentPageLengthUnit;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSelectionMark;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSpan;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentWord;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentPage} instance.
 */
public final class DocumentPageHelper {
    private static DocumentPageAccessor accessor;

    private DocumentPageHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentPage} instance.
     */
    public interface DocumentPageAccessor {
        void setPageNumber(DocumentPage documentPage, int pageNumber);
        void setAngle(DocumentPage documentPage, Float angle);
        void setWidth(DocumentPage documentPage, Float width);
        void setHeight(DocumentPage documentPage, Float height);
        void setUnit(DocumentPage documentPage, DocumentPageLengthUnit unit);
        void setSpans(DocumentPage documentPage, List<DocumentSpan> spans);
        void setWords(DocumentPage documentPage, List<DocumentWord> words);
        void setSelectionMarks(DocumentPage documentPage, List<DocumentSelectionMark> selectionMarks);
        void setLines(DocumentPage documentPage, List<DocumentLine> lines);
        void setKind(DocumentPage documentPage, DocumentPageKind kind);
        void setAnnotations(DocumentPage documentPage, List<DocumentAnnotation> annotations);
        void setBarcodes(DocumentPage documentPage, List<DocumentBarcode> barcodes);
        void setFormulas(DocumentPage documentPage, List<DocumentFormula> formulas);
        void setImages(DocumentPage documentPage, List<DocumentImage> images);

    }

    /**
     * The method called from {@link DocumentPage} to set it's accessor.
     *
     * @param documentPageAccessor The accessor.
     */
    public static void setAccessor(final DocumentPageAccessor documentPageAccessor) {
        accessor = documentPageAccessor;
    }

    static void setPageNumber(DocumentPage documentPage, int pageNumber) {
        accessor.setPageNumber(documentPage, pageNumber);
    }

    static void setAngle(DocumentPage documentPage, Float angle) {
        accessor.setAngle(documentPage, angle);
    }

    static void setWidth(DocumentPage documentPage, Float width) {
        accessor.setWidth(documentPage, width);
    }

    static void setHeight(DocumentPage documentPage, Float height) {
        accessor.setHeight(documentPage, height);
    }
    static void setUnit(DocumentPage documentPage, DocumentPageLengthUnit unit) {
        accessor.setUnit(documentPage, unit);
    }
    static void setSpans(DocumentPage documentPage, List<DocumentSpan> spans) {
        accessor.setSpans(documentPage, spans);
    }
    static void setWords(DocumentPage documentPage, List<DocumentWord> words) {
        accessor.setWords(documentPage, words);
    }
    static void setSelectionMarks(DocumentPage documentPage, List<DocumentSelectionMark> selectionMarks) {
        accessor.setSelectionMarks(documentPage, selectionMarks);
    }
    static void setLines(DocumentPage documentPage, List<DocumentLine> lines) {
        accessor.setLines(documentPage, lines);
    }
    static void setKind(DocumentPage documentPage, DocumentPageKind kind) {
        accessor.setKind(documentPage, kind);
    }
    static void setAnnotations(DocumentPage documentPage, List<DocumentAnnotation> annotations) {
        accessor.setAnnotations(documentPage, annotations);
    }
    static void setBarcodes(DocumentPage documentPage, List<DocumentBarcode> barcodes) {
        accessor.setBarcodes(documentPage, barcodes);
    }
    static void setFormulas(DocumentPage documentPage, List<DocumentFormula> formulas) {
        accessor.setFormulas(documentPage, formulas);
    }
    static void setImages(DocumentPage documentPage, List<DocumentImage> images) {
        accessor.setImages(documentPage, images);
    }
}
