// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The InnerError model.
 */
@Immutable
public final class InnerError {
    /*
     * Error code. Possible values include: 'invalidParameterValue',
     * 'invalidRequestBodyFormat', 'emptyRequest', 'missingInputRecords',
     * 'invalidDocument', 'modelVersionIncorrect', 'invalidDocumentBatch',
     * 'unsupportedLanguageCode', 'invalidCountryHint'
     */
    private final String code;

    /*
     * Error message.
     */
    private final String message;

    /*
     * Error target.
     */
    private final String target;

    /*
     * Inner error contains more specific information.
     */
    private final InnerError innererror;

    /**
     * Creates a {@code InnerError} model that describes inner error information.
     *
     * @param code error code
     * @param message error message
     * @param target error target
     * @param innerError inner error contains more specific information
     */
    public InnerError(String code, String message, String target, InnerError innerError) {
        this.code = code;
        this.message = message;
        this.target = target;
        this.innererror = innerError;
    }

    /**
     * Get the code property: Error code. Possible values include:
     * 'invalidParameterValue', 'invalidRequestBodyFormat', 'emptyRequest',
     * 'missingInputRecords', 'invalidDocument', 'modelVersionIncorrect',
     * 'invalidDocumentBatch', 'unsupportedLanguageCode', 'invalidCountryHint'.
     *
     * @return the code value.
     */
    public String getCode() {
        return this.code;
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
     * Get the target property: Error target.
     *
     * @return the target value.
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * Get the innererror property: Inner error contains more specific
     * information.
     *
     * @return the innererror value.
     */
    public InnerError getInnererror() {
        return this.innererror;
    }
}
