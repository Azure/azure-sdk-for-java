// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.file.datalake.models.DataLakeStorageException;

import java.util.function.Supplier;

public class DataLakeImplUtils {
    public static String endpointToDesiredEndpoint(String endpoint, String desiredEndpoint, String currentEndpoint) {
        // Add the . on either side to prevent overwriting an account name.
        String desiredStringToMatch = "." + desiredEndpoint + ".";
        String currentRegexToMatch = "\\." + currentEndpoint + "\\.";
        if (endpoint.contains(desiredStringToMatch)) {
            return endpoint;
        } else {
            return endpoint.replaceFirst(currentRegexToMatch, desiredStringToMatch);
        }
    }

    public static Throwable transformBlobStorageException(Throwable ex) {
        if (!(ex instanceof BlobStorageException)) {
            return ex;
        } else {
            BlobStorageException exception = (BlobStorageException) ex;
            return new DataLakeStorageException(exception.getServiceMessage(), exception.getResponse(),
                exception.getValue());
        }
    }

    public static <T> T returnOrConvertException(Supplier<T> supplier, ClientLogger logger) {
        try {
            return supplier.get();
        } catch (BlobStorageException ex) {
            throw logger.logExceptionAsError((RuntimeException) transformBlobStorageException(ex));
        }
    }
}
