// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class FlatMapSequentialTests {
    @Test
    public void testFlatMapSequential() {
        Mono<String> mono = Mono.just("1");
        Flux<String> complete = Flux.just("2", "3");
        Function<String, Publisher<String>> onNext = s -> Mono.delay(Duration.ofMillis(10)).thenReturn(s);
        Function<Throwable, Publisher<String>> onError = e -> Mono.just(e.getMessage());
        Supplier<Publisher<String>> onComplete = () -> complete;

        // commented due to it cannot guarantee the time sequence
        // StepVerifier.create(
        //     mono.flatMapMany(onNext, onError, onComplete)
        // )
        //     .expectNext("2", "3", "1")
        //     .verifyComplete();

        StepVerifier.create(
            Utils.flatMapSequential(mono.flux(),
                onNext, onError, onComplete)
        )
            .expectNext("1", "2", "3")
            .verifyComplete();

        StepVerifier.create(
            Utils.flatMapSequential(mono.flux().concatWith(Flux.error(new RuntimeException("Test"))),
                onNext, onError, onComplete)
        )
            .expectNext("1", "Test")
            .verifyComplete();
    }

    @Test
    public void testFlatMapSequentialDelayError() {
        List<Flux<Integer>> flux = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            if (i == 3) {
                flux.add(Flux.error(new RuntimeException("Test")));
            }
            flux.add(Flux.just(i));
        }

        StepVerifier.create(
            Utils.flatMapSequential(
                Flux.range(0, flux.size()),
                flux::get,
                null, null
            )
        )
            .expectNext(0, 1, 2)
            .verifyErrorMessage("Test");

        StepVerifier.create(
            Utils.flatMapSequentialDelayError(
                Flux.range(0, flux.size()),
                flux::get,
                null, null, 32, 32
            )
        )
            .expectNext(0, 1, 2, 3, 4)
            .verifyErrorMessage("Test");

        // commented due to it cannot guarantee the time sequence
        // StepVerifier.create(Flux.just(1, 3, 5, 7)
        //     .flatMapDelayError(i -> {
        //         if (i == 5) {
        //             throw new RuntimeException("Test");
        //         }
        //         return Mono.just(i);
        //     }, 32, 32)
        // )
        //     .expectNext(1, 3)
        //     .verifyErrorMessage("Test");

        // StepVerifier.create(Flux.just(1, 3, 5, 7)
        //     .flatMapDelayError(i -> {
        //         if (i == 5) {
        //             return Mono.error(new RuntimeException("Test"));
        //         }
        //         return Mono.just(i);
        //     }, 32, 32)
        // )
        //     .expectNext(1, 3, 7)
        //     .verifyErrorMessage("Test");
    }
}
