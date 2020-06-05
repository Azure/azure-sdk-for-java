// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.TestUtils;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBodyContent;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionResponse;
import com.azure.messaging.servicebus.models.QueueDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for {@link ServiceBusManagementClientImpl}.
 */
class ServiceBusManagementClientImplIntegrationTests extends TestBase {
    private final ClientLogger logger = new ClientLogger(ServiceBusManagementClientImplIntegrationTests.class);
    private final ServiceBusManagementSerializer serializer = new ServiceBusManagementSerializer();
    private final Duration timeout = Duration.ofSeconds(30);

    private QueuesImpl queuesClient;
    private ServiceBusManagementClientImpl managementClient;

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
        ConnectionStringProperties properties = new ConnectionStringProperties(TestUtils.getConnectionString());
        ServiceBusSharedKeyCredential credential = new ServiceBusSharedKeyCredential(
            properties.getSharedAccessKeyName(), properties.getSharedAccessKey());

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(
            new UserAgentPolicy(),
            new ServiceBusTokenCredentialHttpPolicy(credential),
            new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addAllowedQueryParamName("api-version")),
            new RetryPolicy()
        ).build();

        managementClient = new ServiceBusManagementClientImplBuilder()
            .serializer(serializer)
            .endpoint(properties.getEndpoint().getHost())
            .apiVersion("2017-04")
            .pipeline(pipeline)
            .buildClient();

        queuesClient = managementClient.getQueues();
    }

    /**
     * Verifies we can get queue information.
     */
    @Test
    void getQueue() {
        // Arrange
        String queueName = TestUtils.getQueueName();

        // Act & Assert
        StepVerifier.create(queuesClient.getWithResponseAsync(queueName, true, Context.NONE))
            .assertNext(response -> {
                final QueueDescriptionResponse deserialize = deserialize(response, QueueDescriptionResponse.class);
                assertNotNull(deserialize);
                assertNotNull(deserialize.getContent());

                final QueueDescription properties = deserialize.getContent().getQueueDescription();
                assertNotNull(properties);
                assertFalse(properties.getLockDuration().isZero());
            })
            .verifyComplete();
    }

    /**
     * Verifies we can create a queue.
     */
    @Test
    void createQueue() {
        // Arrange
        String queueName = testResourceNamer.randomName("test", 7);
        QueueDescription description = new QueueDescription().setMaxDeliveryCount(15);
        CreateQueueBody createEntity = new CreateQueueBody();
        CreateQueueBodyContent content = new CreateQueueBodyContent()
            .setType("application/xml")
            .setQueueDescription(description);
        createEntity.setContent(content);

        logger.info("Creating queue: {}", queueName);

        // Act & Assert
        StepVerifier.create(queuesClient.putWithResponseAsync(queueName, createEntity, null, Context.NONE))
            .assertNext(response -> {
                Object body = response.getValue();
                QueueDescription deserialize = null;
                try {
                    deserialize = new ServiceBusManagementSerializer()
                        .deserialize(String.valueOf(body), QueueDescription.class);
                } catch (IOException e) {
                    fail("An exception was thrown. " + e);
                }

                assertNotNull(deserialize);
            })
            .verifyComplete();
    }

    /**
     * Verifies we can delete a queue.
     */
    @Test
    void deleteQueue() {
        // Arrange
        String queueName = testResourceNamer.randomName("test", 7);
        QueueDescription description = new QueueDescription().setMaxDeliveryCount(15);
        CreateQueueBody createEntity = new CreateQueueBody();
        CreateQueueBodyContent content = new CreateQueueBodyContent()
            .setType("application/xml")
            .setQueueDescription(description);
        createEntity.setContent(content);

        logger.info("Creating queue: {}", queueName);

        // This is not part of the scenario. We'll ensure it is created.
        Response<Object> response = queuesClient.putWithResponseAsync(queueName, createEntity, null, Context.NONE)
            .block(timeout);
        assertNotNull(response);

        // Act & Assert
        StepVerifier.create(queuesClient.deleteWithResponseAsync(queueName, Context.NONE))
            .assertNext(deletedResponse -> {
                assertEquals(200, deletedResponse.getStatusCode());
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can edit properties on an existing queue.
     */
    @Test
    void editQueue() {
        // Arrange
        final String queueName = "q-5";
        final Response<Object> response = queuesClient.getWithResponseAsync(queueName, true, Context.NONE)
            .block(Duration.ofSeconds(30));
        assertNotNull(response);
        final QueueDescriptionResponse deserialize = deserialize(response, QueueDescriptionResponse.class);
        final QueueDescription properties = deserialize.getContent().getQueueDescription();

        final int maxDeliveryCount = properties.getMaxDeliveryCount();
        final int newDeliveryCount = maxDeliveryCount + 5;
        final Duration lockDuration = properties.getLockDuration();
        final Duration newLockDuration = lockDuration.plusSeconds(40);
        final Duration autoDeleteOnIdle = Duration.ofDays(5);

        // Set the updated queue properties.
        properties.setMaxDeliveryCount(newDeliveryCount);
        properties.setLockDuration(newLockDuration);
        properties.setAutoDeleteOnIdle(autoDeleteOnIdle);

        CreateQueueBody updated = new CreateQueueBody().setContent(
            new CreateQueueBodyContent().setQueueDescription(properties).setType("application/xml"));

        // Act & Assert
        StepVerifier.create(queuesClient.putWithResponseAsync(queueName, updated, "*", Context.NONE))
            .assertNext(update -> {
                final QueueDescriptionResponse updatedProperties = deserialize(update, QueueDescriptionResponse.class);
                assertNotNull(updatedProperties);
            }).verifyComplete();
    }

    /**
     * Verifies we can list queues.
     */
    @Test
    void listQueues() {
        // Arrange
        String entityType = "queues";

        // Act & Assert
        StepVerifier.create(managementClient.listEntitiesWithResponseAsync(entityType, 0, 100, Context.NONE))
            .assertNext(response -> {
                Object body = response.getValue();
                QueueDescriptionFeed deserialize = null;
                try {
                    deserialize = new ServiceBusManagementSerializer()
                        .deserialize(String.valueOf(body), QueueDescriptionFeed.class);
                } catch (IOException e) {
                    fail("An exception was thrown. " + e);
                }

                assertNotNull(deserialize);
                assertNotNull(deserialize.getEntry());
                assertTrue(deserialize.getEntry().size() > 2);
            })
            .verifyComplete();
    }

    private <T> T deserialize(Response<Object> response, Class<T> clazz) {
        final Object body = response.getValue();
        final String contents = String.valueOf(body);
        final T deserialize;
        try {
            deserialize = serializer.deserialize(contents, clazz);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(String.format(
                "Exception while deserializing. Body: [%s]. Class: %s", contents, clazz), e));
        }

        if (deserialize == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'deserialize' should not be null. Body: [%s]. Class: [%s]", contents, clazz)));
        }

        return deserialize;
    }
}
