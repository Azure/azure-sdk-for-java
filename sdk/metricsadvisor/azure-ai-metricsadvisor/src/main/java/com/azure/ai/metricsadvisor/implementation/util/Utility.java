// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

/**
 * Helper utility class to manage common methods.
 */
public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);

    /**
     * Extracts the result ID from the location URL.
     *
     * @param operationLocation The URL specified in the 'Location' response header containing the
     * resultId used to track the progress and obtain the result of the operation.
     *
     * @return The resultId used to track the progress.
     */
    public static String parseOperationId(String operationLocation) {
        if (!CoreUtils.isNullOrEmpty(operationLocation)) {
            int lastIndex = operationLocation.lastIndexOf('/');
            if (lastIndex != -1) {
                return operationLocation.substring(lastIndex + 1);
            }
        }
        throw LOGGER.logExceptionAsError(
            new RuntimeException("Failed to parse operation header for result Id from: " + operationLocation));
    }
}
