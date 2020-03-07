// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.nio.AzureFileSystemProvider;

import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Only a minimal Utility class to get around a shortcoming in Core's logging.
 */
class LoggingUtility {
    public static <T extends Exception> T logError(ClientLogger logger, T e) {
        logger.error(e.getMessage());
        return e;
    }
}
