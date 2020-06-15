// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.exception.AzureException;

import java.util.List;

/**
 * General exception for FormRecognizer client-side related failures.
 *
 * @see ErrorInformation
 */
public class FormRecognizerException extends AzureException {
    private final List<ErrorInformation> errorInformationList;
    private final String errorInformationMessage;

    /**
     * Initializes a new instance of {@link FormRecognizerException} class
     *
     * @param message Text containing the details of the exception.
     * @param errorInformationList The List of error information that caused the exception
     */
    public FormRecognizerException(final String message, final List<ErrorInformation> errorInformationList) {
        super(message);
        StringBuilder errorInformationStringBuilder = new StringBuilder().append(message);
        if (errorInformationList.size() > 0) {
            for (ErrorInformation errorInformation : errorInformationList) {
                errorInformationStringBuilder.append(", " + "errorCode" + ": [" + errorInformation.getCode()
                    + "], " + "message" + ": " + errorInformation.getMessage());
            }
        }
        this.errorInformationMessage = errorInformationStringBuilder.toString();
        this.errorInformationList = errorInformationList;
    }

    @Override
    public String getMessage() {
        return this.errorInformationMessage;
    }

    /**
     * Get the error information list for this exception.
     *
     * @return the error information list for this exception.
     */
    public List<ErrorInformation> getErrorInformation() {
        return this.errorInformationList;
    }
}
