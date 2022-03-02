// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.administration.models.DocumentModelOperationError;
import com.azure.core.exception.AzureException;

/**
 * Exception for failures related to errors encountered during document analysis, or creation
 * operation.
 *
 * @see DocumentModelOperationError
 */
public final class DocumentModelOperationException extends AzureException {

    /**
     * the error information for this exception.
     */
    private final DocumentModelOperationError documentModelOperationError;

    /**
     * Constructs a new DocumentModelOperationException
     *
     * @param documentModelOperationError the documentModelOperationError underlying this exception
     */
    public DocumentModelOperationException(DocumentModelOperationError documentModelOperationError) {
        super(documentModelOperationError.getInnerError() != null
            ? documentModelOperationError.getInnerError().getMessage()
            : documentModelOperationError.getMessage());
        this.documentModelOperationError = documentModelOperationError;
    }

    /**
     * Get the error information for this exception.
     *
     * @return the error information for this exception.
     */
    public DocumentModelOperationError getDocumentModelOperationError() {
        return this.documentModelOperationError;
    }
}
