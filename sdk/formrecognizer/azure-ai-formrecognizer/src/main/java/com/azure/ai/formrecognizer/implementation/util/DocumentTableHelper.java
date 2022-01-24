// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.BoundingRegion;
import com.azure.ai.formrecognizer.models.DocumentTableCell;
import com.azure.ai.formrecognizer.models.DocumentSpan;
import com.azure.ai.formrecognizer.models.DocumentTable;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentTable} instance.
 */
public final class DocumentTableHelper {
    private static DocumentTableAccessor accessor;

    private DocumentTableHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentTable} instance.
     */
    public interface DocumentTableAccessor {
        void setRowCount(DocumentTable documentTable, int rowCount);

        void setColumnCount(DocumentTable documentTable, int columnCount);

        void setCells(DocumentTable documentTable, List<DocumentTableCell> cells);

        void setBoundingRegions(DocumentTable documentTable, List<BoundingRegion> boundingRegions);

        void setSpans(DocumentTable documentTable, List<DocumentSpan> spans);
    }

    /**
     * The method called from {@link DocumentTable} to set it's accessor.
     *
     * @param documentTableAccessor The accessor.
     */
    public static void setAccessor(final DocumentTableAccessor documentTableAccessor) {
        accessor = documentTableAccessor;
    }

    static void setRowCount(DocumentTable documentTable, int rowCount) {
        accessor.setRowCount(documentTable, rowCount);
    }

    static void setColumnCount(DocumentTable documentTable, int columnCount) {
        accessor.setColumnCount(documentTable, columnCount);
    }

    static void setCells(DocumentTable documentTable, List<DocumentTableCell> cells) {
        accessor.setCells(documentTable, cells);
    }

    static void setBoundingRegions(DocumentTable documentTable, List<BoundingRegion> boundingRegions) {
        accessor.setBoundingRegions(documentTable, boundingRegions);
    }

    static void setSpans(DocumentTable documentTable, List<DocumentSpan> spans) {
        accessor.setSpans(documentTable, spans);
    }
}
