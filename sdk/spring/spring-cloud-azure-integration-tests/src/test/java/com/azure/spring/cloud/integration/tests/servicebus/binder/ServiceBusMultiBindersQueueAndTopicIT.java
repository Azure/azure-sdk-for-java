// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.servicebus.binder;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Sinks;

import java.util.concurrent.CountDownLatch;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("servicebus-multi-binders")
@Import({
    TestServiceBusClientConfiguration.class,
    TestServiceBusMultiBinders.TestQueueConfig.class,
    TestServiceBusMultiBinders.TestTopicConfig.class
})
class ServiceBusMultiBindersQueueAndTopicIT extends TestServiceBusMultiBinders {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusMultiBindersQueueAndTopicIT.class);

    @Autowired
    private Environment environment;

    @Autowired
    private Sinks.Many<Message<String>> manyQueue;

    @Autowired
    private Sinks.Many<Message<String>> manyTopic;

    @Test
    void useMultiBindersQueueAndTopic() throws InterruptedException {
        String activeProfile = String.join("", environment.getActiveProfiles());
        LATCH.put(activeProfile, new CountDownLatch(2));
        LOGGER.info("ServiceBusMultiBindersQueueAndTopicIT begin.");
        exchangeMessageAndVerify(activeProfile, manyQueue, manyTopic);
        LOGGER.info("ServiceBusMultiBindersQueueAndTopicIT end.");
    }

}
