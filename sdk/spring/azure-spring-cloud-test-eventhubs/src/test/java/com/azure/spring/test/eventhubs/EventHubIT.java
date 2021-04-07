// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.eventhubs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class EventHubIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubIT.class);

    private static final String MESSAGE = "Azure Spring Cloud EventHub Test";

    @Autowired
    private Sinks.Many<Message<String>> many;

    @Test
    void integrationTest() {
        ExecutorService executorService = null;
        boolean hasException = false;
        try {
            executorService = Executors.newFixedThreadPool(1);
            Future<?> future = executorService.submit(() -> {
                try {
                    // Wait for eventhub initialization to complete
                    Thread.sleep(9000);
                    many.emitNext(new GenericMessage<>(MESSAGE), Sinks.EmitFailureHandler.FAIL_FAST);
                    Thread.sleep(6000);
                    String msg = Application.EXCHANGER.exchange(MESSAGE);
                    Assertions.assertEquals(MESSAGE, msg);
                    msg = Application.EXCHANGER.exchange("");
                    Assertions.assertEquals("ERROR!", msg);
                } catch (InterruptedException e) {
                    LOGGER.error("InterruptedException found:", e);
                    new RuntimeException(e);
                }
            });
            future.get(35, TimeUnit.SECONDS);
        } catch (Exception ex) {
            hasException = true;
            LOGGER.error("Exception found:", ex);
            if (executorService != null) {
                executorService.shutdown();
            }
        } finally {
            Assertions.assertEquals(hasException, false);
        }
    }
}
