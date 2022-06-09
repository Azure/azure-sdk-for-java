// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentModelOperationErrorHelper;

import java.util.List;

/**
 * The Error information related to the document model creation or analysis operation.
 */
public final class DocumentModelOperationError {
    /*
     * Error code.
     */
    private String code;

    /*
     * Error message.
     */
    private String message;

    /*
     * Target of the error.
     */
    private String target;

    /*
     * List of detailed errors.
     */
    private List<DocumentModelOperationError> details;

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
     * @return the Error object itself.
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
     * @return the Error object itself.
     */
    void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the target property: Target of the error.
     *
     * @return the target value.
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * Set the target property: Target of the error.
     *
     * @param target the target value to set.
     * @return the Error object itself.
     */
    void setTarget(String target) {
        this.target = target;
    }

    /**
     * Get the details property: List of detailed errors.
     *
     * @return the details value.
     */
    public List<DocumentModelOperationError> getDetails() {
        return this.details;
    }

    /**
     * Set the details property: List of detailed errors.
     *
     * @param details the details value to set.
     * @return the Error object itself.
     */
    void setDetails(List<DocumentModelOperationError> details) {
        this.details = details;
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
     * @return the Error object itself.
     */
    void setInnerError(DocumentModelOperationInnerError innerError) {
        this.innerError = innerError;
    }

    static {
        DocumentModelOperationErrorHelper.setAccessor(new DocumentModelOperationErrorHelper.DocumentModelOperationErrorAccessor() {
            @Override
            public void setCode(DocumentModelOperationError documentModelOperationError, String errorCode) {
                documentModelOperationError.setCode(errorCode);
            }

            @Override
            public void setMessage(DocumentModelOperationError documentModelOperationError, String message) {
                documentModelOperationError.setMessage(message);
            }

            @Override
            public void setTarget(DocumentModelOperationError documentModelOperationError, String target) {
                documentModelOperationError.setTarget(target);
            }

            @Override
            public void setDetails(DocumentModelOperationError documentModelOperationError, List<DocumentModelOperationError> details) {
                documentModelOperationError.setDetails(details);
            }

            @Override
            public void setInnerError(DocumentModelOperationError documentModelOperationError, DocumentModelOperationInnerError innererror) {
                documentModelOperationError.setInnerError(innererror);
            }
        });
    }
}
