// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.eventhubs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Sinks;

//@EnableBinding(Source.class)
@SpringBootTest
class EventHubIT {

    private static final String MESSAGE = "Azure Spring Cloud EventHub Test";

    @Autowired
    private Sinks.Many<Message<String>> many;

    @Test
    void integrationTest() throws InterruptedException {
        // Wait for eventhub initialization to complete
        Thread.sleep(9000);
        many.emitNext(new GenericMessage<>(MESSAGE), Sinks.EmitFailureHandler.FAIL_FAST);
        Thread.sleep(6000);
        String msg = Application.EXCHANGER.exchange(MESSAGE);
        Assertions.assertEquals(MESSAGE, msg);
        msg = Application.EXCHANGER.exchange("");
        Assertions.assertEquals("ERROR!", msg);
    }
}
