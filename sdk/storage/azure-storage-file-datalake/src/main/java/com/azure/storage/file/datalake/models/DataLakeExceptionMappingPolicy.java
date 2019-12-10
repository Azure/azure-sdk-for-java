// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.blob.models.BlobStorageException;
import reactor.core.publisher.Mono;

/**
 * This is an exception mapping policy in an HttpPipeline to map BlobStorageExceptions to DataLakeStorageExceptions.
 */
public class DataLakeExceptionMappingPolicy implements HttpPipelinePolicy {

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().onErrorMap(DataLakeExceptionMappingPolicy::transformBlobStorageException);

    }

    /*p
     * Remaps the exception returned from a BlobStorageException to a DataLakeStorageException
     *
     * @param throwable BlobStorageException.
     *
     * @return Exception remapped to a DataLakeStorageException if the throwable was a BlobStorageException,
     * otherwise the throwable is returned unmodified.
     */
    private static Throwable transformBlobStorageException(Throwable throwable) {
        if (!(throwable instanceof BlobStorageException)) {
            return throwable;
        }

        BlobStorageException blobStorageException = (BlobStorageException) throwable;
        return new DataLakeStorageException(blobStorageException.getServiceMessage(),
            blobStorageException.getResponse(), blobStorageException.getValue());
    }
}
