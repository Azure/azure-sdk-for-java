// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelComposeOperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;

/**
 * The helper class to set the non-public properties of an {@link DocumentModelComposeOperationDetails} instance.
 */
public final class DocumentModelComposeOperationDetailsHelper {
    private static DocumentModelComposeOperationDetailsAccessor accessor;

    private DocumentModelComposeOperationDetailsHelper() {
    }

    /**
     * The method called from {@link DocumentModelComposeOperationDetails} to set it's accessor.
     *
     * @param addressValueAccessor The accessor.
     */
    public static void setAccessor(final DocumentModelComposeOperationDetailsHelper.DocumentModelComposeOperationDetailsAccessor addressValueAccessor) {
        accessor = addressValueAccessor;
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentModelComposeOperationDetails} instance.
     */
    public interface DocumentModelComposeOperationDetailsAccessor {
        void setResult(DocumentModelComposeOperationDetails operationDetails, DocumentModelDetails result);
    }

    static void setResult(DocumentModelComposeOperationDetails operationDetails, DocumentModelDetails result) {
        accessor.setResult(operationDetails, result);
    }
}
