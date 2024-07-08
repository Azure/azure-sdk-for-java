// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.rest.Response;
import io.clientcore.core.http.rest.SimpleResponse;
import com.azure.core.v2.util.serializer.TypeReference;
import org.junit.jupiter.api.Test;

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
        Supplier<Response<TestPollResult>>> activationOperation = () -> Mono.fromCallable(() -> {
            activationCallCount[0]++;
            return new SimpleResponse<>(new HttpRequest(HttpMethod.GET, "http://localhost"), 200, new HttpHeaders(),
                new TestPollResult("ActivationDone"));
        });

        PollerFlux<TestPollResult, TestPollResult> pollerFlux
            = PollerFlux.create(Duration.ofMillis(1), activationOperation::get, new StatusCheckPollingStrategy<>(),
                POLL_RESULT_TYPE_REFERENCE, POLL_RESULT_TYPE_REFERENCE);

        StepVerifier.create(pollerFlux)
            .expectSubscription()
            .expectNextMatches(
                asyncPollResponse -> asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .verifyComplete();

        assertEquals(1, activationCallCount[0]);
    }

}
