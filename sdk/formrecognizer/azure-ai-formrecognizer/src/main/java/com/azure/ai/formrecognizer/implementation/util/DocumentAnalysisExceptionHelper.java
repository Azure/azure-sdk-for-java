// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.DocumentAnalysisException;
import com.azure.ai.formrecognizer.administration.models.FormRecognizerError;

/**
 * The helper class to set the non-public properties of an {@link DocumentAnalysisException} instance.
 */
public final class DocumentAnalysisExceptionHelper {
    private static DocumentAnalysisExceptionAccessor accessor;

    private DocumentAnalysisExceptionHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentAnalysisException} instance.
     */
    public interface DocumentAnalysisExceptionAccessor {
        void setErrorInformation(DocumentAnalysisException documentAnalysisException, FormRecognizerError formRecognizerError);
    }

    /**
     * The method called from {@link DocumentAnalysisException} to set it's accessor.
     *
     * @param documentAnalysisExceptionAccessor The accessor.
     */
    public static void setAccessor(final DocumentAnalysisExceptionHelper.DocumentAnalysisExceptionAccessor documentAnalysisExceptionAccessor) {
        accessor = documentAnalysisExceptionAccessor;
    }

    static void setErrorInformation(DocumentAnalysisException documentAnalysisException, FormRecognizerError formRecognizerError) {
        accessor.setErrorInformation(documentAnalysisException, formRecognizerError);
    }
}
