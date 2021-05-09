package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.perf.models.EventHubsOptions;
import com.microsoft.azure.eventhubs.EventDataBatch;
import com.microsoft.azure.eventhubs.EventHubException;
import reactor.core.publisher.Mono;

/**
 * Sends an event data batch with {@link EventHubsOptions#getCount()} number of events in the batch.
 */
public class SendEventDataBatchTest extends ServiceTest {

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    SendEventDataBatchTest(EventHubsOptions options) {
        super(options);
    }

    @Override
    public void run() {
        client = createEventHubClient();

        final EventDataBatch batch = createBatch(client);
        try {
            client.sendSync(batch);
        } catch (EventHubException e) {
            throw new RuntimeException("Unable to send EventDataBatch.", e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.fromCompletionStage(clientFuture
            .thenComposeAsync(client -> {
                final EventDataBatch batch = createBatch(client);
                return client.send(batch);
            }));
    }
}
