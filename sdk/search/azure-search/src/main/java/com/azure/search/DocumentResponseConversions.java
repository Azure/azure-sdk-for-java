// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;

/**
 * Internal utility class for Document Response conversions.
 */
class DocumentResponseConversions {

    private static final String ODATA_CONTEXT = "@odata.context";

    /**
     * Map exceptions to be more informative
     *
     * @param throwable to convert
     * @return Throwable
     */
    static Throwable exceptionMapper(Throwable throwable) {
        if (throwable instanceof HttpResponseException
            && throwable.getMessage().equals("Status code 404, (empty body)")) {
            return new ResourceNotFoundException("Document not found",
                ((HttpResponseException) throwable).getResponse());
        }
        return throwable;
    }

    /**
     * Drop fields that shouldn't be in the returned object
     *
     * @param document document object
     */
    static void cleanupDocument(Document document) {
        document.remove(ODATA_CONTEXT);
    }
}
