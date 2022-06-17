// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.BoundingRegion;
import com.azure.ai.formrecognizer.models.DocumentFootnote;
import com.azure.ai.formrecognizer.models.DocumentSpan;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentFootnote} instance.
 */
public final class DocumentFootnoteHelper {
    private static DocumentFootnoteAccessor accessor;

    private DocumentFootnoteHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentFootnote} instance.
     */
    public interface DocumentFootnoteAccessor {
        void setContent(DocumentFootnote documentFootnote, String content);
        void setSpans(DocumentFootnote documentFootnote, List<DocumentSpan> spans);
        void setBoundingRegions(DocumentFootnote documentFootnote, List<BoundingRegion> boundingRegions);
    }

    /**
     * The method called from {@link DocumentFootnote} to set it's accessor.
     *
     * @param documentFootnoteAccessor The accessor.
     */
    public static void setAccessor(final DocumentFootnoteHelper.DocumentFootnoteAccessor documentFootnoteAccessor) {
        accessor = documentFootnoteAccessor;
    }

    static void setContent(DocumentFootnote documentFootnote, String content) {
        accessor.setContent(documentFootnote, content);
    }

    static void setSpans(DocumentFootnote documentFootnote, List<DocumentSpan> spans) {
        accessor.setSpans(documentFootnote, spans);
    }

    static void setBoundingRegions(DocumentFootnote documentFootnote, List<BoundingRegion> boundingRegions) {
        accessor.setBoundingRegions(documentFootnote, boundingRegions);
    }

}
