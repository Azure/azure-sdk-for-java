// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.exception.ResourceExistsException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.messaging.servicebus.models.QueueDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceBusManagementAsyncClientIntegrationTest extends TestBase {

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    void getQueue(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementAsyncClient client = createClient(httpClient);
        final String queueName = TestUtils.getEntityName(TestUtils.getQueueBaseName(), 5);

        // Act & Assert
        StepVerifier.create(client.getQueue(queueName))
            .assertNext(queueDescription -> {
                assertEquals(queueName, queueDescription.getName());
                assertFalse(queueDescription.isRequiresSession());
                assertNotNull(queueDescription.getLockDuration());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    void createQueueExistingName(HttpClient httpClient) {
        // Arrange
        String queueName = TestUtils.getEntityName(TestUtils.getQueueBaseName(), 5);
        QueueDescription queueDescription = new QueueDescription().setName(queueName);
        final ServiceBusManagementAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.createQueue(queueDescription))
            .consumeErrorWith(error -> {
                assertTrue(error instanceof ResourceExistsException);
            })
            .verify();
    }

    private ServiceBusManagementAsyncClient createClient(HttpClient httpClient) {
        final String connectionString = interceptorManager.isPlaybackMode()
            ? "Endpoint=sb://foo.servicebus.windows.net;SharedAccessKeyName=dummyKey;SharedAccessKey=dummyAccessKey"
            : TestUtils.getConnectionString();
        final ServiceBusManagementClientBuilder builder = new ServiceBusManagementClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .connectionString(connectionString)
            .httpClient(httpClient);

        if (!interceptorManager.isPlaybackMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
            builder.addPolicy(new RetryPolicy());
        }

        return builder.buildAsyncClient();
    }
}
