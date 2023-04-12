// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFormula;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFormulaKind;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSpan;

/**
 * The helper class to set the non-public properties of an {@link DocumentFormula} instance.
 */
public final class DocumentFormulaHelper {
    private static DocumentFormulaAccessor accessor;

    private DocumentFormulaHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentFormula} instance.
     */
    public interface DocumentFormulaAccessor {
        void setSpan(DocumentFormula documentFormula, DocumentSpan span);

        void setKind(DocumentFormula documentFormula, DocumentFormulaKind kind);

        void setValue(DocumentFormula documentFormula, String value);


        void setConfidence(DocumentFormula documentFormula, float confidence);
    }

    /**
     * The method called from {@link DocumentFormula} to set it's accessor.
     *
     * @param documentFormulaAccessor The accessor.
     */
    public static void setAccessor(final DocumentFormulaHelper.DocumentFormulaAccessor documentFormulaAccessor) {
        accessor = documentFormulaAccessor;
    }

    static void setSpan(DocumentFormula documentFormula, DocumentSpan span) {
        accessor.setSpan(documentFormula, span);
    }

    static void setKind(DocumentFormula documentFormula, DocumentFormulaKind kind) {
        accessor.setKind(documentFormula, kind);
    }

    static void setConfidence(DocumentFormula documentFormula, float confidence) {
        accessor.setConfidence(documentFormula, confidence);
    }

    static void setValue(DocumentFormula documentFormula, String value) {
        accessor.setValue(documentFormula, value);
    }
}
