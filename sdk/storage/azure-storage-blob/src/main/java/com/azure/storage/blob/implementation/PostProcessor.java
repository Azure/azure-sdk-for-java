// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation;

import com.azure.storage.blob.models.StorageErrorException;
import com.azure.storage.blob.models.StorageException;
import com.azure.storage.common.Utility;
import reactor.core.publisher.Mono;

/**
 * This class is a helper class that offers functionality to apply processing to responses returned from the service.
 */
public final class PostProcessor {

    /**
     * Applies cleaning to eTag returned from the service if the request was successful, otherwise maps the returned
     * {@link StorageErrorException} to a {@link StorageException} which promotes information from the returned error.
     *
     * @param response Response from the service
     * @param <T> Generic value contained in the response
     * @return The cleansed success response or the mapped exception
     */
    public static <T> Mono<T> postProcessResponse(Mono<T> response) {
        return Utility.postProcessResponse(response, (errorResponse) ->
            errorResponse.onErrorResume(StorageErrorException.class, resume ->
                resume.getResponse()
                    .getBodyAsString()
                    .switchIfEmpty(Mono.just(""))
                    .flatMap(body -> Mono.error(new StorageException(resume, body)))
            ));
    }

    private PostProcessor() {
    }
}
