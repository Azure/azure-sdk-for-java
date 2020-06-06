package com.azure.messaging.servicebus;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceBusManagementAsyncClientIntegrationTest extends TestBase {
    private ServiceBusManagementAsyncClient client;

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
        String queueName = TestUtils.getQueueName();

        // Act & Assert
        StepVerifier.create(client.getQueue(queueName))
            .assertNext(queueDescription -> {
                assertEquals(queueName, queueDescription.getName());
            })
            .verifyComplete();
    }
}
