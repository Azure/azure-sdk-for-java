// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhub.stream.binder.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EventHubBinderRecordModeIT.TestConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties",
        properties = "spring.cloud.stream.eventhub.bindings.input.consumer.checkpoint-mode=RECORD")
public class EventHubBinderRecordModeIT {

    private static String message = UUID.randomUUID().toString();
    @Autowired
    Source source;

    @Test
    public void testSendAndReceiveMessage() {
        this.source.output().send(new GenericMessage<>(message));
    }

    @EnableBinding({Source.class, Sink.class})
    @EnableAutoConfiguration
    public static class TestConfig {

        @StreamListener(Sink.INPUT)
        public void handleMessage(String message) {
            assertThat(message.equals(EventHubBinderRecordModeIT.message)).isTrue();
        }

    }
}
