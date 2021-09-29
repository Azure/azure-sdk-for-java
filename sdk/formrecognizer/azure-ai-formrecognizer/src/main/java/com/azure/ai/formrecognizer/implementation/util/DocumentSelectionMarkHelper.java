// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.DocumentSelectionMark;
import com.azure.ai.formrecognizer.models.SelectionMarkState;
import com.azure.ai.formrecognizer.models.DocumentSpan;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentSelectionMark} instance.
 */
public final class DocumentSelectionMarkHelper {
    private static DocumentSelectionMarkAccessor accessor;

    private DocumentSelectionMarkHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentSelectionMark} instance.
     */
    public interface DocumentSelectionMarkAccessor {
        void setState(DocumentSelectionMark documentSelectionMark, SelectionMarkState state);
        void setBoundingBox(DocumentSelectionMark documentSelectionMark, List<Float> boundingBox);
        void setSpan(DocumentSelectionMark documentSelectionMark, DocumentSpan span);
        void setConfidence(DocumentSelectionMark documentSelectionMark, float confidence);

    }

    /**
     * The method called from {@link DocumentSelectionMark} to set it's accessor.
     *
     * @param documentPageAccessor The accessor.
     */
    public static void setAccessor(final DocumentSelectionMarkAccessor documentPageAccessor) {
        accessor = documentPageAccessor;
    }

    static void setState(DocumentSelectionMark documentSelectionMark, SelectionMarkState state) {
        accessor.setState(documentSelectionMark, state);
    }

    static void setBoundingBox(DocumentSelectionMark documentSelectionMark, List<Float> boundingBox) {
        accessor.setBoundingBox(documentSelectionMark, boundingBox);
    }

    static void setSpan(DocumentSelectionMark documentSelectionMark, DocumentSpan span) {
        accessor.setSpan(documentSelectionMark, span);
    }

    static void setConfidence(DocumentSelectionMark documentSelectionMark, float confidence) {
        accessor.setConfidence(documentSelectionMark, confidence);
    }
}
