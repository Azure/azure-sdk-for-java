// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.exception.AzureException;
import com.azure.core.util.CoreUtils;

import java.util.Collections;
import java.util.List;

/**
 * General exception for FormRecognizer client-side related failures.
 *
 * @see FormRecognizerErrorInformation
 */
public class FormRecognizerException extends AzureException {
    private final List<FormRecognizerErrorInformation> errorInformationList;
    private final String errorInformationMessage;

    /**
     * Initializes a new instance of {@link FormRecognizerException} class
     *
     * @param message Text containing the details of the exception.
     * @param errorInformationList The List of error information that caused the exception
     */
    public FormRecognizerException(final String message,
        final List<FormRecognizerErrorInformation> errorInformationList) {
        super(message);
        StringBuilder errorInformationStringBuilder = new StringBuilder().append(message);
        if (!CoreUtils.isNullOrEmpty(errorInformationList)) {
            for (FormRecognizerErrorInformation errorInformation : errorInformationList) {
                errorInformationStringBuilder.append(", " + "errorCode" + ": [")
                    .append(errorInformation.getErrorCode()).append("], ").append("message")
                    .append(": ").append(errorInformation.getMessage());
            }
            this.errorInformationList = Collections.unmodifiableList(errorInformationList);
        } else {
            this.errorInformationList = null;
        }
        this.errorInformationMessage = errorInformationStringBuilder.toString();
    }

    @Override
    public String getMessage() {
        return this.errorInformationMessage;
    }

    /**
     * Get the error information list for this exception.
     *
     * @return the unmodifiable error information list for this exception.
     */
    public List<FormRecognizerErrorInformation> getErrorInformation() {
        return this.errorInformationList;
    }
}
