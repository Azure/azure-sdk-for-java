package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.perf.models.EventHubsOptions;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import reactor.core.publisher.Mono;

public class EventProcessorClientTest extends ServiceTest {
    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    EventProcessorClientTest(EventHubsOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> setupAsync() {
        final ConnectionStringBuilder connectionStringBuilder = getConnectionStringBuilder();
        final EventProcessorHost.EventProcessorHostBuilder.ManagerStep builder =
            EventProcessorHost.EventProcessorHostBuilder.newBuilder(
                connectionStringBuilder.getEndpoint().toString(), options.getConsumerGroup());

        builder.useUserCheckpointAndLeaseManagers()
        return super.setupAsync();
    }

    @Override
    public void run() {

    }

    @Override
    public Mono<Void> runAsync() {
        return null;
    }
}
