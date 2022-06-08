// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.BoundingRegion;
import com.azure.ai.formrecognizer.models.DocumentParagraph;
import com.azure.ai.formrecognizer.models.DocumentSpan;
import com.azure.ai.formrecognizer.models.ParagraphRole;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentParagraph} instance.
 */
public final class DocumentParagraphHelper {
    private static DocumentParagraphAccessor accessor;

    private DocumentParagraphHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentParagraph} instance.
     */
    public interface DocumentParagraphAccessor {
        void setBoundingRegions(DocumentParagraph documentParagraph, List<BoundingRegion> boundingRegions);
        void setSpans(DocumentParagraph documentParagraph, List<DocumentSpan> spans);
        void setContent(DocumentParagraph documentParagraph, String content);
        void setRole(DocumentParagraph documentParagraph, ParagraphRole role);
    }

    /**
     * The method called from {@link DocumentParagraph} to set it's accessor.
     *
     * @param documentParagraphAccessor The accessor.
     */
    public static void setAccessor(final DocumentParagraphAccessor documentParagraphAccessor) {
        accessor = documentParagraphAccessor;
    }

    static void setContent(DocumentParagraph documentParagraph, String content) {
        accessor.setContent(documentParagraph, content);
    }

    static void setSpans(DocumentParagraph documentParagraph,  List<DocumentSpan> spans) {
        accessor.setSpans(documentParagraph, spans);
    }

    static void setBoundingRegions(DocumentParagraph documentParagraph, List<BoundingRegion> boundingRegions) {
        accessor.setBoundingRegions(documentParagraph, boundingRegions);
    }

    static void setRole(DocumentParagraph documentParagraph, ParagraphRole role) {
        accessor.setRole(documentParagraph, role);
    }
}
