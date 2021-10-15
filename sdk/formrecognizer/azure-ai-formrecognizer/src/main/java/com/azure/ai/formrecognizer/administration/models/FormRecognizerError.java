// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.ai.formrecognizer.implementation.util.FormRecognizerErrorHelper;

import java.util.List;

/**
 * Form Recognizer Error info.
 */
public final class FormRecognizerError {
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
    private List<FormRecognizerError> details;

    /*
     * Detailed error.
     */
    private InnerError innerError;

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
    public List<FormRecognizerError> getDetails() {
        return this.details;
    }

    /**
     * Set the details property: List of detailed errors.
     *
     * @param details the details value to set.
     * @return the Error object itself.
     */
    void setDetails(List<FormRecognizerError> details) {
        this.details = details;
    }

    /**
     * Get the innerError property: Detailed error.
     *
     * @return the innerError value.
     */
    public InnerError getInnerError() {
        return this.innerError;
    }

    /**
     * Set the innerError property: Detailed error.
     *
     * @param innerError the innerError value to set.
     * @return the Error object itself.
     */
    void setInnerError(InnerError innerError) {
        this.innerError = innerError;
    }

    static {
        FormRecognizerErrorHelper.setAccessor(new FormRecognizerErrorHelper.FormRecognizerErrorAccessor() {
            @Override
            public void setCode(FormRecognizerError formRecognizerError, String errorCode) {
                formRecognizerError.setCode(errorCode);
            }

            @Override
            public void setMessage(FormRecognizerError formRecognizerError, String message) {
                formRecognizerError.setMessage(message);
            }

            @Override
            public void setTarget(FormRecognizerError formRecognizerError, String target) {
                formRecognizerError.setTarget(target);
            }

            @Override
            public void setDetails(FormRecognizerError formRecognizerError, List<FormRecognizerError> details) {
                formRecognizerError.setDetails(details);
            }

            @Override
            public void setInnerError(FormRecognizerError formRecognizerError, InnerError innererror) {
                formRecognizerError.setInnerError(innererror);
            }
        });
    }
}
