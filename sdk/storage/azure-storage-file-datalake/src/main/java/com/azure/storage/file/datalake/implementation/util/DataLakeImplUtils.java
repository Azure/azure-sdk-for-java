// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import reactor.core.Exceptions;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DataLakeImplUtils {
    /**
     * Error code the service returns when a path being created already exists.
     */
    public static final String PATH_ALREADY_EXISTS_ERROR_CODE = "PathAlreadyExists";

    /**
     * Determines whether a failure is a 409 Conflict that the service answered with the given error code.
     *
     * <p>The status code alone does not say why the service refused a request: it also answers 409 for reasons such as
     * PathConflict or ContainerBeingDeleted. Methods that treat a conflict as "already exists" use this so that other
     * conflicts still fail.</p>
     *
     * @param throwable The failure.
     * @param errorCode The error code of the conflict to match.
     * @return Whether the failure is a {@link DataLakeStorageException} with status code 409 and the given error code.
     */
    public static boolean isConflictWithErrorCode(Throwable throwable, String errorCode) {
        return throwable instanceof DataLakeStorageException
            && ((DataLakeStorageException) throwable).getStatusCode() == 409
            && errorCode.equals(((DataLakeStorageException) throwable).getErrorCode());
    }

    public static String endpointToDesiredEndpoint(String endpoint, String desiredEndpoint, String currentEndpoint) {
        return StorageImplUtils.endpointToDesiredEndpoint(endpoint, desiredEndpoint, currentEndpoint);
    }

    public static Throwable transformBlobStorageException(Throwable ex) {
        if (ex instanceof BlobStorageException) {
            return transformSingleBlobStorageException((BlobStorageException) ex);
        } else if (Exceptions.isMultiple(ex)) {
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
        return new DataLakeStorageException(ex.getServiceMessage(), ex.getResponse(), ex.getValue());
    }

    public static <T> T returnOrConvertException(Supplier<T> supplier, ClientLogger logger) {
        try {
            return supplier.get();
        } catch (BlobStorageException ex) {
            throw logger.logExceptionAsError((RuntimeException) transformBlobStorageException(ex));
        }
    }

}
