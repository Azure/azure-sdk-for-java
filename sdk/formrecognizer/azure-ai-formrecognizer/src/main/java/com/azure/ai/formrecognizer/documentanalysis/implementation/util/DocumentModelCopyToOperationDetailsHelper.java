// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelCopyToOperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.models.AddressValue;

/**
 * The helper class to set the non-public properties of an {@link DocumentModelCopyToOperationDetails} instance.
 */
public final class DocumentModelCopyToOperationDetailsHelper {
    private static DocumentModelCopyToOperationDetailsAccessor accessor;

    private DocumentModelCopyToOperationDetailsHelper() {
    }

    /**
     * The method called from {@link AddressValue} to set it's accessor.
     *
     * @param addressValueAccessor The accessor.
     */
    public static void setAccessor(final DocumentModelCopyToOperationDetailsHelper.DocumentModelCopyToOperationDetailsAccessor addressValueAccessor) {
        accessor = addressValueAccessor;
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentModelCopyToOperationDetails} instance.
     */
    public interface DocumentModelCopyToOperationDetailsAccessor {
        void setResult(DocumentModelCopyToOperationDetails operationDetails, DocumentModelDetails result);
    }

    static void setResult(DocumentModelCopyToOperationDetails operationDetails, DocumentModelDetails result) {
        accessor.setResult(operationDetails, result);
    }
}
