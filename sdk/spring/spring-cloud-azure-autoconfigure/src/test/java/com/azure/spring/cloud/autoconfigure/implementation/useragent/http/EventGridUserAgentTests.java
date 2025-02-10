// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.useragent.http;


import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherAsyncClient;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventgrid.AzureEventGridAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.eventgrid.properties.AzureEventGridProperties;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.eventgrid.factory.EventGridPublisherClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated("Run this by itself as it captures System.out")
@ExtendWith(OutputCaptureExtension.class)
class EventGridUserAgentTests {

    @Test
    @SuppressWarnings("unchecked")
    void userAgentTest(CapturedOutput output) {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureEventGridAutoConfiguration.class))
            .withPropertyValues(
                "spring.cloud.azure.eventgrid.endpoint=https://sample.somelocation.eventgrid.azure.net/api/eventseventgrid.azure.net/api/events",
                "spring.cloud.azure.eventgrid.key=some-key",
                "spring.cloud.azure.eventgrid.client.logging.level=headers",
                "spring.cloud.azure.eventgrid.client.logging.allowed-header-names=User-Agent",
                "spring.cloud.azure.eventgrid.retry.fixed.delay=1",
                "spring.cloud.azure.eventgrid.retry.fixed.max-retries=0",
                "spring.cloud.azure.eventgrid.retry.mode=fixed"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventGridAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureEventGridProperties.class);
                assertThat(context).hasSingleBean(EventGridPublisherClientBuilderFactory.class);
                assertThat(context).hasSingleBean(EventGridPublisherClientBuilder.class);
                assertThat(context).hasSingleBean(EventGridPublisherClient.class);
                assertThat(context).hasSingleBean(EventGridPublisherAsyncClient.class);

                EventGridPublisherClient<EventGridEvent> eventGridPublisherClient =
                    (EventGridPublisherClient<EventGridEvent>) context.getBean(EventGridPublisherClient.class);
                try {
                    EventGridEvent event = new EventGridEvent("A user is created", "User.Created.Object",
                        BinaryData.fromObject("user1"), "0.1");  // topic must be set when sending to an Event Grid
                    // Domain.
                    eventGridPublisherClient.sendEvent(event);
                } catch (Exception exception) {
                    // Eat it because we just want the log.
                }
                String allOutput = output.getAll();
                String format1 = String.format("User-Agent:%s", AzureSpringIdentifier.AZURE_SPRING_EVENT_GRID);
                String format2 = String.format("\"User-Agent\":\"%s",
                    AzureSpringIdentifier.AZURE_SPRING_EVENT_GRID);
                assertTrue(allOutput.contains(format1) || allOutput.contains(format2));
            });
    }
}
