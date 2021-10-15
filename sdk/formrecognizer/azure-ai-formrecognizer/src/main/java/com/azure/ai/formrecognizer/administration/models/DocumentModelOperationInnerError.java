// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.ai.formrecognizer.implementation.util.InnerErrorHelper;

/**
 * Detailed error.
 */
public final class DocumentModelOperationInnerError {
    /*
     * Error code.
     */
    private String code;

    /*
     * Error message.
     */
    private String message;

    /*
     * Detailed error.
     */
    private DocumentModelOperationInnerError innerError;

    /**
     * Get the code property: Error code.
     *
     * @return the code value.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Set the code property: Error code.
     *
     * @param code the code value to set.
     * @return the DocumentModelOperationInnerError object itself.
     */
    void setCode(String code) {
        this.code = code;
    }

    /**
     * Get the message property: Error message.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Set the message property: Error message.
     *
     * @param message the message value to set.
     * @return the DocumentModelOperationInnerError object itself.
     */
    void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the innerError property: Detailed error.
     *
     * @return the innerError value.
     */
    public DocumentModelOperationInnerError getInnerError() {
        return this.innerError;
    }

    /**
     * Set the innerError property: Detailed error.
     *
     * @param innerError the innerError value to set.
     * @return the DocumentModelOperationInnerError object itself.
     */
    void setInnerError(DocumentModelOperationInnerError innerError) {
        this.innerError = innerError;
    }

    static {
        InnerErrorHelper.setAccessor(new InnerErrorHelper.InnerErrorAccessor() {
            @Override
            public void setCode(DocumentModelOperationInnerError innerError, String errorCode) {
                innerError.setCode(errorCode);
            }

            @Override
            public void setMessage(DocumentModelOperationInnerError innerError, String message) {
                innerError.setMessage(message);
            }

            @Override
            public void setInnerError(DocumentModelOperationInnerError innerError, DocumentModelOperationInnerError innererror) {
                innerError.setInnerError(innerError);
            }
        });
    }
}
