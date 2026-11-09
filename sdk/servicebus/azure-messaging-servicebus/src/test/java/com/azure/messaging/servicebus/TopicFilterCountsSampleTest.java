// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.implementation.EntityHelper;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescription;
import com.azure.messaging.servicebus.administration.models.TopicProperties;
import com.azure.messaging.servicebus.administration.models.TopicRuntimeProperties;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class TopicFilterCountsSampleTest {
    @Test
    void printsFilterCountsFromExistingTopic() throws UnsupportedEncodingException {
        TopicDescription description = new TopicDescription().setSqlFilterCount(7).setCorrelationFilterCount(9);
        TopicProperties topic = EntityHelper.toModel(description);
        EntityHelper.setTopicName(topic, "orders");
        ServiceBusAdministrationClient client = mock(ServiceBusAdministrationClient.class);
        when(client.getTopicRuntimeProperties("orders")).thenReturn(new TopicRuntimeProperties(topic));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(buffer, true, "UTF-8")) {
            TopicFilterCountsSample.printFilterCounts(client, "orders", output);
        }

        assertEquals(String.format("Topic: orders, SQL filters: 7, correlation filters: 9%n"),
            new String(buffer.toByteArray(), StandardCharsets.UTF_8));
        verify(client).getTopicRuntimeProperties("orders");
        verifyNoMoreInteractions(client);
    }
}
