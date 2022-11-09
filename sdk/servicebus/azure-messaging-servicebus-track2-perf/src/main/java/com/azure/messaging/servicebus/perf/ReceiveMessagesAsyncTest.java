package com.azure.messaging.servicebus.perf;

import reactor.core.publisher.Mono;

/**
 * Test ServiceBus receiver async client receive messages performance.
 */
public class ReceiveMessagesAsyncTest extends ServiceBusEventTest<ServiceBusStressOptions> {
    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public ReceiveMessagesAsyncTest(ServiceBusStressOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(Mono.defer(() -> {
            receiveMessages = receiverAsync.receiveMessages().flatMap(message -> {
                eventRaised();
                if (!options.getIsDeleteMode()) {
                    return receiverAsync.complete(message);
                }
                return Mono.empty();
            }).subscribe();
            return Mono.empty();
        }));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.defer(() -> {
            receiveMessages.dispose();
            receiverAsync.close();
            return Mono.empty();
        }).then(super.cleanupAsync());
    }
}
