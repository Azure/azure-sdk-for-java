// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 * 
 **/
public class ObservableHelper {

    static public <T> Mono<T> inlineIfPossible(Callable<Mono<T>> function, IRetryPolicy retryPolicy) {

        if (retryPolicy == null) {
            // shortcut
            try {
                return function.call();
            } catch (Exception e) {
                return Mono.error(e);
            }
        } else {
            return BackoffRetryUtility.executeRetry(function, retryPolicy);
        }
    }

    static public <T> Flux<T> inlineIfPossibleAsObs(Callable<Flux<T>> function, IRetryPolicy retryPolicy) {

        if (retryPolicy == null) {
            // shortcut
            return Flux.defer(() -> {
                try {
                    return function.call();
                } catch (Exception e) {
                    return Flux.error(e);
                }
            });

        } else {
            return BackoffRetryUtility.executeRetry(() -> function.call().single(), retryPolicy).flux();
        }
    }
}
