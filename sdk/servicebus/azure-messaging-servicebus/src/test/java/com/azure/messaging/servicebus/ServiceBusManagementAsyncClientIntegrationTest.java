// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.messaging.servicebus.implementation.models.ServiceBusManagementErrorException;
import com.azure.messaging.servicebus.models.QueueDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceBusManagementAsyncClientIntegrationTest extends TestBase {
    private ServiceBusManagementAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @Override
    protected void beforeTest() {
        Assumptions.assumeTrue(getTestMode() != TestMode.PLAYBACK,
            "Current record/playback does not support persisting XML calls.");

        client = new ServiceBusManagementClientBuilder()
            .connectionString(TestUtils.getConnectionString())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient();
    }

    @Test
    void getQueue() {
        // Arrange
        String queueName = TestUtils.getQueueBaseName();

        // Act & Assert
        StepVerifier.create(client.getQueue(queueName))
            .assertNext(queueDescription -> {
                assertEquals(queueName, queueDescription.getName());
            })
            .verifyComplete();
    }

    @Test
    void createQueueExistingName() {
        // Arrange
        String queueName = TestUtils.getEntityName(TestUtils.getQueueBaseName(), 5);
        QueueDescription queueDescription = new QueueDescription().setName(queueName);

        // Act & Assert
        StepVerifier.create(client.createQueue(queueDescription))
            .consumeErrorWith(error -> {
                assertTrue(error instanceof ServiceBusManagementErrorException);
            })
            .verify();
    }
}
