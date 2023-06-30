// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentModelCopyToOperationDetailsHelper;
import com.azure.core.annotation.Immutable;

/** Copy document model operation details. */
@Immutable
public final class DocumentModelCopyToOperationDetails extends OperationDetails {
    /*
     * Operation result upon success.
     */
    private DocumentModelDetails result;

    /**
     * Get the result property: Operation result upon success.
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
    private void setResult(
        DocumentModelDetails result) {
        this.result = result;
    }

    static {
        DocumentModelCopyToOperationDetailsHelper.setAccessor(
            new DocumentModelCopyToOperationDetailsHelper.DocumentModelCopyToOperationDetailsAccessor() {
                public void setResult(DocumentModelCopyToOperationDetails operationDetails,
                                      DocumentModelDetails result) {
                    operationDetails.setResult(result);
                }
            });
    }
}
