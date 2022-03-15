// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.useragent.amqp;

import com.azure.core.util.ClientOptions;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubsAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhubs.TestCheckpointStore;
import com.azure.spring.cloud.autoconfigure.useragent.util.UserAgentTestUtil;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsErrorHandler;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsRecordMessageListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class EventHubsUserAgentTests {

    @Test
    void userAgentTest() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureEventHubsAutoConfiguration.class))
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(EventHubsRecordMessageListener.class, () -> message -> { })
            .withBean(EventHubsErrorHandler.class, () -> errorContext -> { })
            .withBean(CheckpointStore.class, TestCheckpointStore::new)
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=sample",
                "spring.cloud.azure.eventhubs.event-hub-name=sample",
                "spring.cloud.azure.eventhubs.processor.consumer-group=sample",
                "spring.cloud.azure.eventhubs.consumer.consumer-group=sample"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsAutoConfiguration.class);
                assertThat(context).hasSingleBean(EventHubConsumerAsyncClient.class);
                assertThat(context).hasSingleBean(EventHubConsumerClient.class);

                EventProcessorClientBuilder eventProcessorClientBuilder = context.getBean(EventProcessorClientBuilder.class);
                EventHubClientBuilder eventHubClientBuilder = (EventHubClientBuilder) UserAgentTestUtil.getPrivateFieldValue(EventProcessorClientBuilder.class, "eventHubClientBuilder", eventProcessorClientBuilder);
                ClientOptions options = (ClientOptions) UserAgentTestUtil.getPrivateFieldValue(EventHubClientBuilder.class, "clientOptions", eventHubClientBuilder);
                Assertions.assertNotNull(options);
                Assertions.assertEquals(AzureSpringIdentifier.AZURE_SPRING_EVENT_HUBS, options.getApplicationId());

            });
    }

}
