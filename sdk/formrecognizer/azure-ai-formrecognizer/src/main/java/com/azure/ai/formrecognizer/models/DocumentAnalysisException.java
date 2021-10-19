// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.administration.models.FormRecognizerError;
import com.azure.ai.formrecognizer.implementation.util.DocumentAnalysisExceptionHelper;
import com.azure.core.exception.AzureException;

/**
 * General exception for Document analysis client-side related failures.
 *
 * @see FormRecognizerError
 */
public final class DocumentAnalysisException extends AzureException {

    private FormRecognizerError errorInformation;

    /**
     * Get the error information for this exception.
     *
     * @return the error information for this exception.
     */
    public FormRecognizerError getErrorInformation() {
        return this.errorInformation;
    }

    /**
     * Set the error information for this exception.
     */
    void setErrorInformation(FormRecognizerError errorInformation) {
        this.errorInformation = errorInformation;
    }

    static {
        DocumentAnalysisExceptionHelper.setAccessor(
            (documentAnalysisException, formRecognizerError) -> documentAnalysisException.setErrorInformation(formRecognizerError));
    }
}
