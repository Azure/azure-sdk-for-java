// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentModelComposeOperationDetailsHelper;
import com.azure.core.annotation.Immutable;

/** Compose document model operation details */
@Immutable
public final class DocumentModelComposeOperationDetails extends OperationDetails {

    /**
     * Creates a DocumentModelComposeOperationDetails object.
     */
    public DocumentModelComposeOperationDetails() {
        super();
    }

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
        DocumentModelComposeOperationDetailsHelper
            .setAccessor(new DocumentModelComposeOperationDetailsHelper.DocumentModelComposeOperationDetailsAccessor() {
                public void setResult(DocumentModelComposeOperationDetails operationDetails,
                    DocumentModelDetails result) {
                    operationDetails.setResult(result);
                }
            });
    }
}
