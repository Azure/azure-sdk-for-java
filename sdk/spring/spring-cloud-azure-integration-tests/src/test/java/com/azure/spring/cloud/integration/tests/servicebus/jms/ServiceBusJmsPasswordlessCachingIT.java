// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.integration.tests.servicebus.jms;

import jakarta.jms.ConnectionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.Exchanger;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("servicebus-jms-passwordless-caching")
@Import(TestServiceBusJmsConfiguration.PasswordlessQueueCachingApiConfig.class)
public class ServiceBusJmsPasswordlessCachingIT extends TestServiceBusJmsConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusJmsPasswordlessCachingIT.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ConnectionFactory connectionFactory;

    public ServiceBusJmsPasswordlessCachingIT() {
        EXCHANGER.put(PASSWORDLESS_CACHING_API_QUEUE_NAME, new Exchanger<>());
    }

    @Test
    @Timeout(70)
    void testJmsOperationViaCachingConnection() throws InterruptedException {
        Assertions.assertSame(CachingConnectionFactory.class, connectionFactory.getClass());
        LOGGER.info("ServiceBusJmsPasswordlessCachingIT begin.");
        this.exchangeMessage(jmsTemplate, PASSWORDLESS_CACHING_API_QUEUE_NAME);
        LOGGER.info("ServiceBusJmsPasswordlessCachingIT end.");
    }
}
