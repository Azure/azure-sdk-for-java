// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.DocumentModelOperationException;
import com.azure.ai.formrecognizer.administration.models.DocumentModelOperationError;

/**
 * The helper class to set the non-public properties of an {@link DocumentModelOperationException} instance.
 */
public final class DocumentAnalysisExceptionHelper {
    private static DocumentAnalysisExceptionAccessor accessor;

    private DocumentAnalysisExceptionHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentModelOperationException} instance.
     */
    public interface DocumentAnalysisExceptionAccessor {
        void setErrorInformation(DocumentModelOperationException documentModelOperationException, DocumentModelOperationError documentModelOperationError);
    }

    /**
     * The method called from {@link DocumentModelOperationException} to set it's accessor.
     *
     * @param documentAnalysisExceptionAccessor The accessor.
     */
    public static void setAccessor(final DocumentAnalysisExceptionHelper.DocumentAnalysisExceptionAccessor documentAnalysisExceptionAccessor) {
        accessor = documentAnalysisExceptionAccessor;
    }

    static void setErrorInformation(DocumentModelOperationException documentModelOperationException, DocumentModelOperationError documentModelOperationError) {
        accessor.setErrorInformation(documentModelOperationException, documentModelOperationError);
    }
}
