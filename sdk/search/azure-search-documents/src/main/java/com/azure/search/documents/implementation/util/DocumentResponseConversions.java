// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.implementation.models.SearchErrorException;

/**
 * Utility class for Document Response conversions.
 */
public final class DocumentResponseConversions {
    /*
     * Exception message to use if the document isn't found.
     */
    private static final String DOCUMENT_NOT_FOUND = "Document not found.";

    /**
     * Converts the {@link Throwable} into a more descriptive exception type if the {@link SearchDocument} isn't found.
     *
     * @param throwable Throwable thrown during a API call.
     * @return The {@link Throwable} mapped to a more descriptive exception type if the {@link SearchDocument}
     * isn't found, otherwise the passed {@link Throwable} unmodified.
     */
    public static Throwable exceptionMapper(Throwable throwable) {
        if (!(throwable instanceof SearchErrorException)) {
            return throwable;
        }

        SearchErrorException exception = (SearchErrorException) throwable;
        if (exception.getResponse().getStatusCode() == 404) {
            return new ResourceNotFoundException(DOCUMENT_NOT_FOUND, exception.getResponse());
        }
        return new HttpResponseException(exception.getMessage(), exception.getResponse());
    }

    private DocumentResponseConversions() {
    }
}
