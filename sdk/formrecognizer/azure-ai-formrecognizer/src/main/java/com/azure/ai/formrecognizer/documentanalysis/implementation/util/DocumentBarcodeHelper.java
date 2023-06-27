// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.models.DocumentBarcode;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentBarcodeKind;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSpan;

/**
 * The helper class to set the non-public properties of an {@link DocumentBarcode} instance.
 */
public final class DocumentBarcodeHelper {
    private static DocumentBarcodeAccessor accessor;

    private DocumentBarcodeHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentBarcode} instance.
     */
    public interface DocumentBarcodeAccessor {
        void setSpan(DocumentBarcode documentBarcode, DocumentSpan spans);

        void setKind(DocumentBarcode documentBarcode, DocumentBarcodeKind kind);

        void setValue(DocumentBarcode documentBarcode, String value);


        void setConfidence(DocumentBarcode documentBarcode, float confidence);
    }

    /**
     * The method called from {@link DocumentBarcode} to set it's accessor.
     *
     * @param documentBarcodeAccessor The accessor.
     */
    public static void setAccessor(final DocumentBarcodeHelper.DocumentBarcodeAccessor documentBarcodeAccessor) {
        accessor = documentBarcodeAccessor;
    }

    static void setSpan(DocumentBarcode documentBarcode, DocumentSpan span) {
        accessor.setSpan(documentBarcode, span);
    }

    static void setKind(DocumentBarcode documentBarcode, DocumentBarcodeKind kind) {
        accessor.setKind(documentBarcode, kind);
    }

    static void setConfidence(DocumentBarcode documentBarcode, float confidence) {
        accessor.setConfidence(documentBarcode, confidence);
    }

    static void setValue(DocumentBarcode documentBarcode, String value) {
        accessor.setValue(documentBarcode, value);
    }
}
