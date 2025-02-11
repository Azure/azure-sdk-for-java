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
@ActiveProfiles("servicebus-single-binder-no-inherit")
@Import({
    TestServiceBusClientConfiguration.class,
    TestServiceBusSingleBinder.TestQueueConfig.class,
    TestServiceBusSingleBinder.TestTopicConfig.class
})
class ServiceBusSingleBinderNoInheritQueueAndTopicIT extends TestServiceBusSingleBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusSingleBinderNoInheritQueueAndTopicIT.class);

    @Autowired
    private Environment environment;

    @Autowired
    private Sinks.Many<Message<String>> manyQueue;

    @Autowired
    private Sinks.Many<Message<String>> manyTopic;

    @Test
    void useSingleBindersNoInheritQueueAndTopic() throws InterruptedException {
        String activeProfile = String.join("", environment.getActiveProfiles());
        LATCH.put(activeProfile, new CountDownLatch(2));
        LOGGER.info("ServiceBusSingleBinderNoInheritQueueAndTopicIT begin, the property inherit-environment is set to false.");
        exchangeMessageAndVerify(activeProfile, manyQueue, manyTopic);
        LOGGER.info("ServiceBusSingleBinderNoInheritQueueAndTopicIT end.");
    }

}
