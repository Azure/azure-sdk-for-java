// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.storage.common.Utility;
import com.azure.storage.queue.models.StorageErrorException;
import com.azure.storage.queue.models.StorageException;
import reactor.core.publisher.Mono;

final class PostProcessor {
    static <T> Mono<T> postProcessResponse(Mono<T> response) {
        return Utility.postProcessResponse(response, (errorResponse) ->
            errorResponse.onErrorResume(StorageErrorException.class, resume ->
                resume.response()
                    .bodyAsString()
                    .switchIfEmpty(Mono.just(""))
                    .flatMap(body -> Mono.error(new StorageException(resume, body)))
            ));
    }

    private PostProcessor() {
    }
}
