// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildOperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.models.AddressValue;

/**
 * The helper class to set the non-public properties of an {@link DocumentModelBuildOperationDetails} instance.
 */
public final class DocumentModelBuildOperationDetailsHelper {
    private static DocumentModelBuildOperationDetailsAccessor accessor;

    private DocumentModelBuildOperationDetailsHelper() {
    }

    /**
     * The method called from {@link AddressValue} to set it's accessor.
     *
     * @param addressValueAccessor The accessor.
     */
    public static void setAccessor(final DocumentModelBuildOperationDetailsHelper.DocumentModelBuildOperationDetailsAccessor addressValueAccessor) {
        accessor = addressValueAccessor;
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentModelBuildOperationDetails} instance.
     */
    public interface DocumentModelBuildOperationDetailsAccessor {
        void setResult(DocumentModelBuildOperationDetails operationDetails, DocumentModelDetails result);
    }

    static void setResult(DocumentModelBuildOperationDetails operationDetails, DocumentModelDetails result) {
        accessor.setResult(operationDetails, result);
    }
}
