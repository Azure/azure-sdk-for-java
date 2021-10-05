// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.DocumentLine;
import com.azure.ai.formrecognizer.models.DocumentPage;
import com.azure.ai.formrecognizer.models.DocumentSelectionMark;
import com.azure.ai.formrecognizer.models.DocumentWord;
import com.azure.ai.formrecognizer.models.LengthUnit;
import com.azure.ai.formrecognizer.models.DocumentSpan;

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
        void setAngle(DocumentPage documentPage, float angle);
        void setWidth(DocumentPage documentPage, float width);
        void setHeight(DocumentPage documentPage, float height);
        void setUnit(DocumentPage documentPage, LengthUnit unit);
        void setSpans(DocumentPage documentPage, List<DocumentSpan> spans);
        void setWords(DocumentPage documentPage, List<DocumentWord> words);
        void setSelectionMarks(DocumentPage documentPage, List<DocumentSelectionMark> selectionMarks);
        void setLines(DocumentPage documentPage, List<DocumentLine> lines);
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

    static void setAngle(DocumentPage documentPage, float angle) {
        accessor.setAngle(documentPage, angle);
    }

    static void setWidth(DocumentPage documentPage, float width) {
        accessor.setWidth(documentPage, width);
    }

    static void setHeight(DocumentPage documentPage, float height) {
        accessor.setHeight(documentPage, height);
    }

    static void setUnit(DocumentPage documentPage, LengthUnit unit) {
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
}
