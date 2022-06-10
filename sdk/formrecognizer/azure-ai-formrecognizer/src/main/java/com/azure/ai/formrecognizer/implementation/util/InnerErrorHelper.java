// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.DocumentModelOperationInnerError;

/**
 * The helper class to set the non-public properties of an {@link DocumentModelOperationInnerError} instance.
 */
public final class InnerErrorHelper {
    private static InnerErrorAccessor accessor;

    private InnerErrorHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentModelOperationInnerError} instance.
     */
    public interface InnerErrorAccessor {
        void setCode(DocumentModelOperationInnerError innerError, String errorCode);
        void setMessage(DocumentModelOperationInnerError innerError, String message);
        void setInnerError(DocumentModelOperationInnerError innerError, DocumentModelOperationInnerError serviceInnerError);
    }

    /**
     * The method called from {@link DocumentModelOperationInnerError} to set it's accessor.
     *
     * @param innerErrorAccessor The accessor.
     */
    public static void setAccessor(final InnerErrorAccessor innerErrorAccessor) {
        accessor = innerErrorAccessor;
    }

    static void setCode(DocumentModelOperationInnerError innerError, String errorCode) {
        accessor.setCode(innerError, errorCode);
    }

    static void setMessage(DocumentModelOperationInnerError innerError, String message) {
        accessor.setMessage(innerError, message);
    }

    static void setInnerError(DocumentModelOperationInnerError innerError, DocumentModelOperationInnerError serviceInnerError) {
        accessor.setInnerError(innerError, serviceInnerError);
    }
}
