package com.azure.spring.cloud.docker.compose.implementation.service.connection.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.AzureEventHubsAutoConfiguration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.docker.compose.skip.in-tests=false",
    "spring.docker.compose.file=classpath:com/azure/spring/cloud/docker/compose/implementation/service/connection/eventhubs/eventhubs-compose.yaml",
    "spring.docker.compose.stop.command=down",
    "spring.cloud.azure.eventhubs.event-hub-name=eh1",
    "spring.cloud.azure.eventhubs.consumer.consumer-group=$default"
})
class EventHubsDockerComposeConnectionDetailsFactoryTests {

    @Autowired
    private EventHubProducerClient producerClient;

    @Autowired
    private EventHubConsumerClient consumerClient;

    @Test
    void test() {
        this.producerClient.send(List.of(new EventData("test message")));

        Awaitility.waitAtMost(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            IterableStream<PartitionEvent> events = this.consumerClient.receiveFromPartition("0", 1,
                EventPosition.earliest(), Duration.ofSeconds(2));
            Iterator<PartitionEvent> iterator = events.stream().iterator();
            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next().getData().getBodyAsString()).isEqualTo("test message");
        });
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(classes = {
        AzureGlobalPropertiesAutoConfiguration.class,
        AzureEventHubsAutoConfiguration.class})
    static class Config {
    }

}
