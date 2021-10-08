// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.BoundingRegion;
import com.azure.ai.formrecognizer.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.models.DocumentField;
import com.azure.ai.formrecognizer.models.DocumentSpan;

import java.util.List;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link AnalyzedDocument} instance.
 */
public final class AnalyzedDocumentHelper {
    private static AnalyzedDocumentAccessor accessor;

    private AnalyzedDocumentHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzedDocument} instance.
     */
    public interface AnalyzedDocumentAccessor {
        void setDocType(AnalyzedDocument analyzedDocument, String docType);
        void setBoundingRegions(AnalyzedDocument analyzedDocument, List<BoundingRegion> boundingRegions);
        void setSpans(AnalyzedDocument analyzedDocument, List<DocumentSpan> spans);
        void setFields(AnalyzedDocument analyzedDocument, Map<String, DocumentField> fields);
        void setConfidence(AnalyzedDocument analyzedDocument, float confidence);
    }

    /**
     * The method called from {@link AnalyzedDocument} to set it's accessor.
     *
     * @param analyzedDocumentAccessor The accessor.
     */
    public static void setAccessor(final AnalyzedDocumentHelper.AnalyzedDocumentAccessor analyzedDocumentAccessor) {
        accessor = analyzedDocumentAccessor;
    }

    static void setDocType(AnalyzedDocument analyzedDocument, String docType) {
        accessor.setDocType(analyzedDocument, docType);
    }

    static void setBoundingRegions(AnalyzedDocument analyzedDocument, List<BoundingRegion> boundingRegions) {
        accessor.setBoundingRegions(analyzedDocument, boundingRegions);
    }

    static void setSpans(AnalyzedDocument analyzedDocument, List<DocumentSpan> spans) {
        accessor.setSpans(analyzedDocument, spans);
    }

    static void setFields(AnalyzedDocument analyzedDocument, Map<String, DocumentField> fields) {
        accessor.setFields(analyzedDocument, fields);
    }

    static void setConfidence(AnalyzedDocument analyzedDocument, float confidence) {
        accessor.setConfidence(analyzedDocument, confidence);
    }
}
