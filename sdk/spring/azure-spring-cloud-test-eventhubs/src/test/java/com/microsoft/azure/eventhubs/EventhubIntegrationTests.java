package com.microsoft.azure.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.GenericMessage;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableBinding(Source.class)
@SpringBootTest
class EventhubIntegrationTests {

    private static final String MESSAGE = "Azure Spring Cloud EventHub Test";

    @Value("${spring.cloud.azure.eventhub.connection-string}")
    private String connectionString;

    @Value("${spring.cloud.stream.bindings.input.destination}")
    private String eventHubName;

    @Test
    void integrationTest(@Autowired Source source) {
        source.output().send(new GenericMessage<>(MESSAGE));

        EventHubConsumerClient consumerClient = new EventHubClientBuilder()
            .connectionString(connectionString, eventHubName)
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .buildConsumerClient();

        IterableStream<PartitionEvent> events = consumerClient.receiveFromPartition("0", 1, EventPosition.latest());
        PartitionEvent event = events.stream().findFirst().get();
        assertEquals(MESSAGE, event.getData().getBodyAsString());
    }

}
