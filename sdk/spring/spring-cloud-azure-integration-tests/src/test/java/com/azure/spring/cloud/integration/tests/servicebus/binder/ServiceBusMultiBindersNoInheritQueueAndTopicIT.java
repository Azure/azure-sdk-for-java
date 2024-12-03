// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.servicebus.binder;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Sinks;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("servicebus-multi-binders-no-inherit")
@Import({
    TestServiceBusClientConfiguration.class,
    TestServiceBusMultiBinders.TestQueueConfig.class,
    TestServiceBusMultiBinders.TestTopicConfig.class
})
class ServiceBusMultiBindersNoInheritQueueAndTopicIT extends TestServiceBusMultiBinders {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusMultiBindersNoInheritQueueAndTopicIT.class);

    @Autowired
    private Sinks.Many<Message<String>> manyQueue;

    @Autowired
    private Sinks.Many<Message<String>> manyTopic;

    @Test
    void useMultiBindersNoInheritQueueAndTopic() throws InterruptedException {
        LOGGER.info("ServiceBusMultiBindersNoInheritQueueAndTopicIT begin, the property inherit-environment is set to false for binder servicebus-1.");
        exchangeMessageAndVerify(manyQueue, manyTopic);
        LOGGER.info("ServiceBusMultiBindersNoInheritQueueAndTopicIT end.");
    }
}
