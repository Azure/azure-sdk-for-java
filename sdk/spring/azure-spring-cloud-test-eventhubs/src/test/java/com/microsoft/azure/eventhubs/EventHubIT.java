// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.GenericMessage;

@EnableBinding(Source.class)
@SpringBootTest
class EventHubIT {

    private static final String MESSAGE = "Azure Spring Cloud EventHub Test";

    private final Source source;

    @Autowired
    EventHubIT(Source source) {
        this.source = source;
    }

    @Test
    void integrationTest() throws InterruptedException {
        // Wait for eventhub initialization to complete
        Thread.sleep(15000);
        this.source.output().send(new GenericMessage<>(MESSAGE));
        String msg = Receiver.EXCHANGER.exchange(MESSAGE);
        Assertions.assertEquals(MESSAGE, msg);
        msg = Receiver.EXCHANGER.exchange("");
        Assertions.assertEquals("ERROR!", msg);
    }

}
