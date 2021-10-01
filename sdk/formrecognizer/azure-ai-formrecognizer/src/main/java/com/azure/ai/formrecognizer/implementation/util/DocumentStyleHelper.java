// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.DocumentSpan;
import com.azure.ai.formrecognizer.models.DocumentStyle;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentStyle} instance.
 */
public final class DocumentStyleHelper {
    private static DocumentStyleAccessor accessor;

    private DocumentStyleHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentStyle} instance.
     */
    public interface DocumentStyleAccessor {
        void setSpans(DocumentStyle documentStyle, List<DocumentSpan> spans);

        void setIsHandwritten(DocumentStyle documentStyle, Boolean isHandwritten);

        void setConfidence(DocumentStyle documentStyle, Float confidence);
    }

    /**
     * The method called from {@link DocumentStyle} to set it's accessor.
     *
     * @param documentStyleAccessor The accessor.
     */
    public static void setAccessor(final DocumentStyleHelper.DocumentStyleAccessor documentStyleAccessor) {
        accessor = documentStyleAccessor;
    }

    static void setSpans(DocumentStyle documentStyle, List<DocumentSpan> spans) {
        accessor.setSpans(documentStyle, spans);
    }

    static void setIsHandwritten(DocumentStyle documentStyle, Boolean isHandwritten) {
        accessor.setIsHandwritten(documentStyle, isHandwritten);
    }

    static void setConfidence(DocumentStyle documentStyle, Float confidence) {
        accessor.setConfidence(documentStyle, confidence);
    }
}
