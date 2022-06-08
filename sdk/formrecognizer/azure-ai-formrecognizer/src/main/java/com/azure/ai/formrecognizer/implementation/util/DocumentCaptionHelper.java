// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.BoundingRegion;
import com.azure.ai.formrecognizer.models.DocumentCaption;
import com.azure.ai.formrecognizer.models.DocumentSpan;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentCaption} instance.
 */
public final class DocumentCaptionHelper {
    private static DocumentCaptionAccessor accessor;

    private DocumentCaptionHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentCaption} instance.
     */
    public interface DocumentCaptionAccessor {
        void setContent(DocumentCaption documentCaption, String content);
        void setSpans(DocumentCaption documentCaption, List<DocumentSpan> spans);
        void setBoundingRegions(DocumentCaption documentCaption, List<BoundingRegion> boundingRegions);
    }

    /**
     * The method called from {@link DocumentCaption} to set it's accessor.
     *
     * @param documentCaptionAccessor The accessor.
     */
    public static void setAccessor(final DocumentCaptionHelper.DocumentCaptionAccessor documentCaptionAccessor) {
        accessor = documentCaptionAccessor;
    }

    static void setContent(DocumentCaption documentCaption, String content) {
        accessor.setContent(documentCaption, content);
    }

    static void setSpans(DocumentCaption documentCaption, List<DocumentSpan> spans) {
        accessor.setSpans(documentCaption, spans);
    }

    static void setBoundingRegions(DocumentCaption documentCaption, List<BoundingRegion> boundingRegions) {
        accessor.setBoundingRegions(documentCaption, boundingRegions);
    }

}
