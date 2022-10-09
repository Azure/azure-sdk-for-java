// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.models.DocumentLine;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSpan;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentWord;
import com.azure.ai.formrecognizer.documentanalysis.models.Point;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentLine} instance.
 */
public final class DocumentLineHelper {
    private static DocumentLineAccessor accessor;

    private DocumentLineHelper() {
    }

    /**
     * The method called from {@link DocumentLine} to set it's accessor.
     *
     * @param documentLineAccessor The accessor.
     */
    public static void setAccessor(final DocumentLineHelper.DocumentLineAccessor documentLineAccessor) {
        accessor = documentLineAccessor;
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentLine} instance.
     */
    public interface DocumentLineAccessor {
        void setContent(DocumentLine documentLine, String content);
        void setBoundingPolygon(DocumentLine documentLine, List<Point> boundingPolygon);
        void setSpans(DocumentLine documentLine, List<DocumentSpan> spans);
        void setPageWords(DocumentLine documentLine, List<DocumentWord> pageWords);

    }

    static void setContent(DocumentLine documentLine, String content) {
        accessor.setContent(documentLine, content);
    }

    static void setBoundingPolygon(DocumentLine documentLine, List<Point> boundingPolygon) {
        accessor.setBoundingPolygon(documentLine, boundingPolygon);
    }

    static void setSpans(DocumentLine documentLine, List<DocumentSpan> spans) {
        accessor.setSpans(documentLine, spans);
    }

    static void setPageWords(DocumentLine documentLine, List<DocumentWord> pageWords) {
        accessor.setPageWords(documentLine, pageWords);
    }
}
