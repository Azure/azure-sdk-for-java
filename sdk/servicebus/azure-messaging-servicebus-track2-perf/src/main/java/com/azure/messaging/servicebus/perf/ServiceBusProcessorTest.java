// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import reactor.core.publisher.Mono;

/**
 * Test ServiceBus processor client receive messages performance. Use eventRaised() and errorRaised() to record messages
 * count and error count.
 */
public class ServiceBusProcessorTest extends ServiceBusEventTest<ServiceBusStressOptions> {

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public ServiceBusProcessorTest(ServiceBusStressOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(Mono.defer(() -> {
            processor.start();
            return Mono.empty();
        }));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.defer(() -> {
            processor.stop();
            return Mono.empty();
        }).then(super.cleanupAsync());
    }
}
