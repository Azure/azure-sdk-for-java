// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.integration.tests.servicebus.jms;

import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Exchanger;

abstract class TestServiceBusJmsConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestServiceBusJmsConfiguration.class);
    private static final String DATA = "service bus test data";
    static final String CONNECTION_STRING_POOL_API_QUEUE_NAME = "conn_string_pool_queue";
    static final String PASSWORDLESS_POOL_API_QUEUE_NAME = "passwordless_pool_queue";
    static final String PASSWORDLESS_CACHING_API_QUEUE_NAME = "passwordless_caching_queue";
    static final String PASSWORDLESS_DEFAULT_API_QUEUE_NAME = "passwordless_default_queue";
    static final Map<String, Exchanger<String>> EXCHANGER = new ConcurrentHashMap<>();

    @TestConfiguration
    static class QueuePoolApiConnectionStringConfig {

        @JmsListener(destination = CONNECTION_STRING_POOL_API_QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
        void receiveQueueMessage(String message) throws InterruptedException {
            EXCHANGER.get(CONNECTION_STRING_POOL_API_QUEUE_NAME).exchange(message);
            LOGGER.info("Received message from queue {} (connection string): {}", CONNECTION_STRING_POOL_API_QUEUE_NAME,message);
        }
    }

    @TestConfiguration
    static class PasswordlessQueuePoolApiConfig {
        @JmsListener(destination = PASSWORDLESS_POOL_API_QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
        void receiveQueueMessage(String message) throws InterruptedException {
            EXCHANGER.get(PASSWORDLESS_POOL_API_QUEUE_NAME).exchange(message);
            LOGGER.info("Received message from queue {} (pool api): {}", PASSWORDLESS_POOL_API_QUEUE_NAME,message);
        }
    }

    @TestConfiguration
    static class PasswordlessQueueCachingApiConfig {
        @JmsListener(destination = PASSWORDLESS_CACHING_API_QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
        void receiveQueueMessage(String message) throws InterruptedException {
            EXCHANGER.get(PASSWORDLESS_CACHING_API_QUEUE_NAME).exchange(message);
            LOGGER.info("Received message from queue {} (caching api): {}", PASSWORDLESS_CACHING_API_QUEUE_NAME,message);
        }
    }

    @TestConfiguration
    static class PasswordlessQueuedDefaultApiConfig {
        @JmsListener(destination = PASSWORDLESS_DEFAULT_API_QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
        void receiveQueueMessage(String message) throws InterruptedException {
            EXCHANGER.get(PASSWORDLESS_DEFAULT_API_QUEUE_NAME).exchange(message);
            LOGGER.info("Received message from queue {} (default api): {}", PASSWORDLESS_DEFAULT_API_QUEUE_NAME, message);
        }
    }

    void exchangeMessage(JmsTemplate currentJmsTemplate, String queueName) throws InterruptedException {
        currentJmsTemplate.convertAndSend(queueName, DATA);
        LOGGER.info("Send message for {}: {}", queueName, DATA);
        String msg = EXCHANGER.get(queueName).exchange(null);
        Assertions.assertEquals(DATA, msg);
    }
}
