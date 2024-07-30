// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.function;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;


public final class FunctionUtils {
    public static <A1, A2, A3, A4, A5, A6, A7, TReturn> SevFunction<A1, A2, A3, A4, A5, A6, A7, TReturn> toSyncFunction(
        SevFunction<A1, A2, A3, A4, A5, A6, A7, Mono<TReturn>> asyncFunction) {
        return (A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7) -> {
            Mono<TReturn> mono = asyncFunction.apply(a1, a2, a3, a4, a5, a6, a7);
            mono.subscribe();
            return mono.block();
        };
    }

    public static <TReturn> TReturn callAndAwait(
        Supplier<Mono<TReturn>> asyncFunction) {
        return asyncFunction.get().block();
    }
}

