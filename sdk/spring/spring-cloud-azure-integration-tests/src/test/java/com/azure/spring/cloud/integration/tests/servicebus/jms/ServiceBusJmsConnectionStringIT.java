// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.servicebus.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import jakarta.jms.ConnectionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.Exchanger;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("servicebus-jms-connection-string")
@Import(TestServiceBusJmsConfiguration.QueuePoolApiConnectionStringConfig.class)
public class ServiceBusJmsConnectionStringIT extends TestServiceBusJmsConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusJmsConnectionStringIT.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ConnectionFactory connectionFactory;

    public ServiceBusJmsConnectionStringIT() {
        EXCHANGER.put(CONNECTION_STRING_POOL_API_QUEUE_NAME, new Exchanger<>());
    }

    @Test
    @Timeout(70)
    void testJmsOperationViaConnStringAndCachingConnection() throws InterruptedException {
        Assertions.assertSame(ServiceBusJmsConnectionFactory.class, connectionFactory.getClass());
        LOGGER.info("ServiceBusJmsConnectionStringIT begin.");
        this.exchangeMessage(jmsTemplate, CONNECTION_STRING_POOL_API_QUEUE_NAME);
        LOGGER.info("ServiceBusJmsConnectionStringIT end.");
    }
}
