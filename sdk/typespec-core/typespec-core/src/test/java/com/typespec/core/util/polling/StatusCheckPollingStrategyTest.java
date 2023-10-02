// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.polling;

import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.rest.Response;
import com.typespec.core.http.rest.SimpleResponse;
import com.typespec.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatusCheckPollingStrategyTest {

    private static final TypeReference<TestPollResult> POLL_RESULT_TYPE_REFERENCE
        = TypeReference.createInstance(TestPollResult.class);

    @Test
    public void statusCheckPollingStrategySucceedsOnActivation() {
        int[] activationCallCount = new int[1];
        Supplier<Mono<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.GET, "http://localhost"), 200, new HttpHeaders(),
                new TestPollResult("ActivationDone"));
        });

        PollerFlux<TestPollResult, TestPollResult> pollerFlux = PollerFlux.create(Duration.ofMillis(1),
            activationOperation::get, new StatusCheckPollingStrategy<>(), POLL_RESULT_TYPE_REFERENCE,
            POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

}
