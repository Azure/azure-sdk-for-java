// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio.implementation.util;

import com.azure.core.util.logging.ClientLogger;

/**
 * RESERVED FOR INTERNAL USE
 */
public class Utility {
    public static <T extends Exception> T logError(ClientLogger logger, T e) {
        logger.error(e.getMessage());
        return e;
    }
}
