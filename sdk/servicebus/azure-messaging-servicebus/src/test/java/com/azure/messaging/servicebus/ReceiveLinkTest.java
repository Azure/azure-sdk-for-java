// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.when;

public class ReceiveLinkTest {
    @Mock
    private AmqpReceiveLink link1;
    @Mock
    private AmqpReceiveLink link2;
    @Mock
    private AmqpReceiveLink link3;
    @Mock
    private AmqpReceiveLink link4;
    private final AtomicInteger counter = new AtomicInteger();
    private AmqpReceiveLink[] allLinks = new AmqpReceiveLink[4];

    @Test
    void verifyCreation() {
        MockitoAnnotations.initMocks(this);

        allLinks[0] = link1;
        allLinks[1] = link2;
        allLinks[2] = link3;
        allLinks[3] = link4;

        when(link1.getLinkName()).thenReturn("link1-name");
        when(link1.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.UNINITIALIZED)));

        when(link2.getLinkName()).thenReturn("link2-name");
        when(link2.getEndpointStates()).thenAnswer(invocation -> {
            System.out.println("link2-name endpoints");
            return Flux.create(sink -> {
                final Disposable s = Mono.delay(Duration.ofSeconds(3))
                    .subscribe(e -> sink.next(AmqpEndpointState.UNINITIALIZED));
                sink.onRequest(r -> {
                    sink.next(AmqpEndpointState.UNINITIALIZED);
                });
            });
        });

        when(link3.getLinkName()).thenReturn("link3-name");
        when(link3.getEndpointStates()).thenAnswer(invocation -> {
            System.out.println("link3-name endpoints");
            return Flux.create(sink -> {
                final Disposable s = Mono.delay(Duration.ofSeconds(3))
                    .subscribe(e -> sink.next(AmqpEndpointState.ACTIVE));
                sink.onRequest(r -> {
                    sink.next(AmqpEndpointState.UNINITIALIZED);
                });
            });
        });

        when(link4.getLinkName()).thenReturn("link4-name");
        when(link4.getEndpointStates()).thenReturn(Flux.error(new IllegalArgumentException("Did not expect this.")));

        StepVerifier.create(getActiveLink())
            .assertNext(theLink -> {
                Assertions.assertEquals(link3, theLink);
            })
            .verifyComplete();
    }

    private Mono<AmqpReceiveLink> getActiveLink() {
        return Mono.defer(() -> {
            return createReceiveLink()
                .flatMap(link -> link.getEndpointStates()
                    .subscriberContext(Context.of("linkName", link.getLinkName()))
                    .takeUntil(e -> e == AmqpEndpointState.ACTIVE)
                    .timeout(Duration.ofSeconds(4))
                    .then(Mono.just(link)));
        })
            .retryWhen(Retry.from(retrySignals -> retrySignals.flatMap(signal -> {
                final Throwable failure = signal.failure();
                System.err.printf("    Retry: %s. Error occurred while waiting: %s%n", signal.totalRetriesInARow(), failure);
                if (failure instanceof TimeoutException) {
                    return Mono.delay(Duration.ofSeconds(4));
                } else {
                    return Mono.<Long>error(failure);
                }
            })));
    }

    private Mono<AmqpReceiveLink> createReceiveLink() {
        int index = counter.getAndIncrement();

        System.out.println("Index: " + index);
        if (index < allLinks.length) {
            return Mono.just(allLinks[index]);
        } else {
            return Mono.error(new IllegalArgumentException("Index is too big."));
        }
    }
}
