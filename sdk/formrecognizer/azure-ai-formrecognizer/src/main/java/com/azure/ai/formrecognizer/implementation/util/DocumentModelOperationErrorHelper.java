// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.DocumentModelOperationError;
import com.azure.ai.formrecognizer.administration.models.DocumentModelOperationInnerError;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentModelOperationError} instance.
 */
public final class DocumentModelOperationErrorHelper {
    private static DocumentModelOperationErrorAccessor accessor;

    private DocumentModelOperationErrorHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentModelOperationError} instance.
     */
    public interface DocumentModelOperationErrorAccessor {
        void setCode(DocumentModelOperationError documentModelOperationError, String errorCode);
        void setMessage(DocumentModelOperationError documentModelOperationError, String message);

        void setTarget(DocumentModelOperationError documentModelOperationError, String target);

        void setDetails(DocumentModelOperationError documentModelOperationError, List<DocumentModelOperationError> details);

        void setInnerError(DocumentModelOperationError documentModelOperationError, DocumentModelOperationInnerError innerError);
    }

    /**
     * The method called from {@link DocumentModelOperationError} to set it's accessor.
     *
     * @param documentModelOperationErrorAccessor The accessor.
     */
    public static void setAccessor(final DocumentModelOperationErrorAccessor documentModelOperationErrorAccessor) {
        accessor = documentModelOperationErrorAccessor;
    }

    static void setCode(DocumentModelOperationError documentModelOperationError, String errorCode) {
        accessor.setCode(documentModelOperationError, errorCode);
    }

    static void setMessage(DocumentModelOperationError documentModelOperationError, String message) {
        accessor.setMessage(documentModelOperationError, message);
    }

    static void setTarget(DocumentModelOperationError documentModelOperationError, String target) {
        accessor.setTarget(documentModelOperationError, target);
    }

    static void setDetails(DocumentModelOperationError documentModelOperationError, List<DocumentModelOperationError> details) {
        accessor.setDetails(documentModelOperationError, details);
    }

    static void setInnerError(DocumentModelOperationError documentModelOperationError, DocumentModelOperationInnerError innerError) {
        accessor.setInnerError(documentModelOperationError, innerError);
    }
}
