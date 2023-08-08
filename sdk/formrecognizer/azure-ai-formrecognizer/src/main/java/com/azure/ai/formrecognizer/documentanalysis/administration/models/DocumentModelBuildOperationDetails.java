// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentModelBuildOperationDetailsHelper;
import com.azure.core.annotation.Immutable;

/** Build document model operation details */
@Immutable
public final class DocumentModelBuildOperationDetails extends OperationDetails {
    /*
     * Operation result upon success.
     */
    private DocumentModelDetails result;

    /**
     * Get the operation result upon success.
     *
     * @return the result value.
     */
    public DocumentModelDetails getResult() {
        return this.result;
    }

    /**
     * Set the result property: Operation result upon success.
     *
     * @param result the result value to set.
     */
    private void setResult(DocumentModelDetails result) {
        this.result = result;
    }

    static {
        DocumentModelBuildOperationDetailsHelper.setAccessor(
            new DocumentModelBuildOperationDetailsHelper.DocumentModelBuildOperationDetailsAccessor() {
                public void setResult(DocumentModelBuildOperationDetails operationDetails,
                                      DocumentModelDetails result) {
                    operationDetails.setResult(result);
                }
            });
    }
}
