// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.BoundingRegion;
import com.azure.ai.formrecognizer.models.DocumentTableCell;
import com.azure.ai.formrecognizer.models.DocumentTableCellKind;
import com.azure.ai.formrecognizer.models.DocumentSpan;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentTableCell} instance.
 */
public final class DocumentTableCellHelper {
    private static DocumentTableCellAccessor accessor;

    private DocumentTableCellHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentTableCell} instance.
     */
    public interface DocumentTableCellAccessor {
        void setSpans(DocumentTableCell documentTableCell, List<DocumentSpan> spans);
        void setBoundingRegions(DocumentTableCell documentTableCell, List<BoundingRegion> boundingRegions);

        void setContent(DocumentTableCell documentTableCell, String content);

        void setColumnSpan(DocumentTableCell documentTableCell, Integer columnSpan);

        void setRowSpan(DocumentTableCell documentTableCell, Integer rowSpan);

        void setColumnIndex(DocumentTableCell documentTableCell, int columnIndex);

        void setRowIndex(DocumentTableCell documentTableCell, int rowIndex);

        void setKind(DocumentTableCell documentTableCell, DocumentTableCellKind kind);
    }

    /**
     * The method called from {@link DocumentTableCell} to set it's accessor.
     *
     * @param documentTableCellAccessor The accessor.
     */
    public static void setAccessor(final DocumentTableCellHelper.DocumentTableCellAccessor documentTableCellAccessor) {
        accessor = documentTableCellAccessor;
    }

    static void setSpans(DocumentTableCell documentTableCell, List<DocumentSpan> spans) {
        accessor.setSpans(documentTableCell, spans);
    }

    static void setBoundingRegions(DocumentTableCell documentTableCell, List<BoundingRegion> boundingRegions) {
        accessor.setBoundingRegions(documentTableCell, boundingRegions);
    }

    static void setContent(DocumentTableCell documentTableCell, String content) {
        accessor.setContent(documentTableCell, content);
    }

    static void setColumnSpan(DocumentTableCell documentTableCell, Integer columnSpan) {
        accessor.setColumnSpan(documentTableCell, columnSpan);
    }

    static void setRowSpan(DocumentTableCell documentTableCell, Integer rowSpan) {
        accessor.setRowSpan(documentTableCell, rowSpan);
    }

    static void setColumnIndex(DocumentTableCell documentTableCell, int columnIndex) {
        accessor.setColumnIndex(documentTableCell, columnIndex);
    }

    static void setRowIndex(DocumentTableCell documentTableCell, int rowIndex) {
        accessor.setRowIndex(documentTableCell, rowIndex);
    }

    static void setKind(DocumentTableCell documentTableCell, DocumentTableCellKind kind) {
        accessor.setKind(documentTableCell, kind);
    }
}
