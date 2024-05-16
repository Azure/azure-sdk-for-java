// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSpan;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentStyle;
import com.azure.ai.formrecognizer.documentanalysis.models.FontStyle;
import com.azure.ai.formrecognizer.documentanalysis.models.FontWeight;

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
        void setColor(DocumentStyle documentStyle, String color);
        void setFontWeight(DocumentStyle documentStyle, FontWeight fontWeight);
        void setSimilarFontFamily(DocumentStyle documentStyle, String similarFontFamily);
        void setBackgroundColor(DocumentStyle documentStyle, String backgroundColor);
        void setFontStyle(DocumentStyle documentStyle, FontStyle fontStyle);
    }

    /**
     * The method called from {@link DocumentStyle} to set it's accessor.
     *
     * @param documentStyleAccessor The accessor.
     */
    public static void setAccessor(final DocumentStyleAccessor documentStyleAccessor) {
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

    static void setSimilarFontFamily(DocumentStyle documentStyle, String similarFontFamily) {
        accessor.setSimilarFontFamily(documentStyle, similarFontFamily);
    }

    static void setFontStyle(DocumentStyle documentStyle, FontStyle fontStyle) {
        accessor.setFontStyle(documentStyle, fontStyle);
    }

    static void setFontWeight(DocumentStyle documentStyle, FontWeight fontWeight) {
        accessor.setFontWeight(documentStyle, fontWeight);
    }

    static void setColor(DocumentStyle documentStyle, String color) {
        accessor.setColor(documentStyle, color);
    }

    static void setBackgroundColor(DocumentStyle documentStyle, String backgroundColor) {
        accessor.setBackgroundColor(documentStyle, backgroundColor);
    }
}
