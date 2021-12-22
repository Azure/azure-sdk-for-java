// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.administration.models.DocumentModelOperationError;
import com.azure.ai.formrecognizer.implementation.util.DocumentAnalysisExceptionHelper;
import com.azure.core.exception.AzureException;

/**
 * General exception for client-side related failures related to document model analysis, or creation
 * operation.
 *
 * @see DocumentModelOperationError
 */
public final class DocumentModelOperationException extends AzureException {

    /**
     * the error information for this exception.
     */
    private DocumentModelOperationError documentModelOperationError;

    /**
     * Get the error information for this exception.
     *
     * @return the error information for this exception.
     */
    public DocumentModelOperationError getDocumentModelOperationError() {
        return this.documentModelOperationError;
    }

    /**
     * Set the error information for this exception.
     */
    void setDocumentModelOperationError(DocumentModelOperationError documentModelOperationError) {
        this.documentModelOperationError = documentModelOperationError;
    }

    static {
        DocumentAnalysisExceptionHelper.setAccessor(
            ((documentModelOperationException, documentModelOperationError)
                -> documentModelOperationException.setDocumentModelOperationError(documentModelOperationError)));
    }
}
