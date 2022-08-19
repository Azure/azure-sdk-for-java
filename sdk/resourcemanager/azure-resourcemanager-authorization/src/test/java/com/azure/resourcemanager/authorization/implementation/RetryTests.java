// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.http.HttpResponse;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.RetryBackoffSpec;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryTests {

    @Test
    public void testRetry() {
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(false));

        HttpResponse mockedResponse400 = Mockito.mock(HttpResponse.class);
        Mockito.when(mockedResponse400.getStatusCode()).thenReturn(400);
        HttpResponse mockedResponse404 = Mockito.mock(HttpResponse.class);
        Mockito.when(mockedResponse404.getStatusCode()).thenReturn(404);

        RetryBackoffSpec retry = ActiveDirectoryApplicationImpl.backoffRetryFor404();
        AtomicInteger retryCount = new AtomicInteger(0);
        retry = retry.doAfterRetry(ignored -> {
            System.out.println("retry " + retryCount.incrementAndGet() + ", at " + OffsetDateTime.now());
        });

        Mono<String> monoSuccess = Mono.just("foo");

        StepVerifier.create(monoSuccess.retryWhen(retry))
            .expectSubscription()
            .expectNext("foo")
            .expectComplete()
            .verify();

        Mono<String> monoError400 = Mono.error(new ManagementException("error", mockedResponse400));

        retryCount.set(0);
        StepVerifier.create(monoError400.retryWhen(retry))
            .expectSubscription()
            .expectErrorMatches(e -> e instanceof ManagementException && ((ManagementException) e).getResponse().getStatusCode() == 400)
            .verify();
        Assertions.assertEquals(0, retryCount.get());

        Mono<String> monoError404 = Mono.error(new ManagementException("error", mockedResponse404));

        retryCount.set(0);
        StepVerifier.create(monoError404.retryWhen(retry))
            .expectSubscription()
            .expectErrorMatches(e -> e instanceof ManagementException && ((ManagementException) e).getResponse().getStatusCode() == 404)
            .verify();
        Assertions.assertEquals(3, retryCount.get());

        AtomicInteger errorCount = new AtomicInteger(0);
        Mono<String> monoError404Twice = Mono.create(sink -> {
            if (errorCount.incrementAndGet() <= 2) {
                sink.error(new ManagementException("error", mockedResponse404));
            } else {
                sink.success("foo");
            }
        });

        retryCount.set(0);
        StepVerifier.create(monoError404Twice.retryWhen(retry))
            .expectSubscription()
            .expectNext("foo")
            .expectComplete()
            .verify();
        Assertions.assertEquals(2, retryCount.get());
    }
}
