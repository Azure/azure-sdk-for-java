// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import reactor.core.Exceptions;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
        if (ex instanceof BlobStorageException) {
            return transformSingleBlobStorageException((BlobStorageException) ex);
        } else if(Exceptions.isMultiple(ex)) {
            List<Throwable> suppressed = Exceptions.unwrapMultiple(ex);
            suppressed = suppressed.stream().map(e -> {
                if (e instanceof BlobStorageException) {
                    return transformSingleBlobStorageException((BlobStorageException) e);
                } else {
                    return e;
                }
            }).collect(Collectors.toList());
            return Exceptions.multiple(suppressed);
        } else {
            return ex;
        }
    }

    private static DataLakeStorageException transformSingleBlobStorageException(BlobStorageException ex) {
        return new DataLakeStorageException(ex.getServiceMessage(), ex.getResponse(),
            ex.getValue());
    }

    public static <T> T returnOrConvertException(Supplier<T> supplier, ClientLogger logger) {
        try {
            return supplier.get();
        } catch (BlobStorageException ex) {
            throw logger.logExceptionAsError((RuntimeException) transformBlobStorageException(ex));
        }
    }
}
