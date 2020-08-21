// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.TestUtils;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBodyContent;
import com.azure.messaging.servicebus.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionEntry;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.implementation.models.QueueDescriptionResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for {@link ServiceBusManagementClientImpl}.
 */
class ServiceBusAdministrationClientImplIntegrationTests extends TestBase {
    private final ClientLogger logger = new ClientLogger(ServiceBusAdministrationClientImplIntegrationTests.class);
    private final ServiceBusManagementSerializer serializer = new ServiceBusManagementSerializer();
    private final Duration timeout = Duration.ofSeconds(30);

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    /**
     * Verifies we can get queue information.
     */
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    void getQueueImplementation(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementClientImpl managementClient = createClient(httpClient);
        final EntitiesImpl entityClient = managementClient.getEntities();
        final String queueName = interceptorManager.isPlaybackMode()
            ? "queue-0"
            : TestUtils.getEntityName(TestUtils.getQueueBaseName(), 0);

        // Act & Assert
        StepVerifier.create(entityClient.getWithResponseAsync(queueName, true, Context.NONE))
            .assertNext(response -> {
                final QueueDescriptionEntry deserialize = deserialize(response, QueueDescriptionEntry.class);
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
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    void createQueueImplementation(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementClientImpl managementClient = createClient(httpClient);
        final EntitiesImpl entityClient = managementClient.getEntities();

        final String queueName = testResourceNamer.randomName("test", 7);
        final CreateQueueOptions options = new CreateQueueOptions()
            .setMaxDeliveryCount(15);
        final QueueDescription queueProperties = EntityHelper.getQueueDescription(options);
        final CreateQueueBody createEntity = new CreateQueueBody();
        final CreateQueueBodyContent content = new CreateQueueBodyContent()
            .setType("application/xml")
            .setQueueDescription(queueProperties);
        createEntity.setContent(content);

        logger.info("Creating queue: {}", queueName);

        // Act & Assert
        StepVerifier.create(entityClient.putWithResponseAsync(queueName, createEntity, null, Context.NONE))
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
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    void deleteQueueImplementation(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementClientImpl managementClient = createClient(httpClient);
        final EntitiesImpl entityClient = managementClient.getEntities();

        final String queueName = testResourceNamer.randomName("test", 7);
        final CreateQueueOptions description = new CreateQueueOptions()
            .setMaxDeliveryCount(15);
        final QueueDescription queueProperties = EntityHelper.getQueueDescription(description);
        final CreateQueueBody createEntity = new CreateQueueBody();
        final CreateQueueBodyContent content = new CreateQueueBodyContent()
            .setType("application/xml")
            .setQueueDescription(queueProperties);
        createEntity.setContent(content);

        logger.info("Creating queue: {}", queueName);

        // This is not part of the scenario. We'll ensure it is created.
        Response<Object> response = entityClient.putWithResponseAsync(queueName, createEntity, null, Context.NONE)
            .block(timeout);
        assertNotNull(response);

        // Act & Assert
        StepVerifier.create(entityClient.deleteWithResponseAsync(queueName, Context.NONE))
            .assertNext(deletedResponse -> {
                assertEquals(200, deletedResponse.getStatusCode());
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can edit properties on an existing queue.
     */
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    void editQueueImplementation(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementClientImpl managementClient = createClient(httpClient);
        final EntitiesImpl entityClient = managementClient.getEntities();

        final String queueName = interceptorManager.isPlaybackMode()
            ? "queue-5"
            : TestUtils.getEntityName(TestUtils.getQueueBaseName(), 5);
        final Response<Object> response = entityClient.getWithResponseAsync(queueName, true, Context.NONE)
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
        StepVerifier.create(entityClient.putWithResponseAsync(queueName, updated, "*", Context.NONE))
            .assertNext(update -> {
                final QueueDescriptionResponse updatedProperties = deserialize(update, QueueDescriptionResponse.class);
                assertNotNull(updatedProperties);
            }).verifyComplete();
    }

    /**
     * Verifies we can list queues.
     */
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    void listQueuesImplementation(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementClientImpl managementClient = createClient(httpClient);
        final String entityType = "queues";

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

    private ServiceBusManagementClientImpl createClient(HttpClient httpClient) {
        final String connectionString = interceptorManager.isPlaybackMode()
            ? "Endpoint=sb://foo.servicebus.windows.net;SharedAccessKeyName=dummyKey;SharedAccessKey=dummyAccessKey"
            : TestUtils.getConnectionString();
        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
        final ServiceBusSharedKeyCredential credential = new ServiceBusSharedKeyCredential(
            properties.getSharedAccessKeyName(), properties.getSharedAccessKey());
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy());
        policies.add(new ServiceBusTokenCredentialHttpPolicy(credential));
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        final HttpClient httpClientToUse;
        if (interceptorManager.isPlaybackMode()) {
            httpClientToUse = interceptorManager.getPlaybackClient();
        } else {
            httpClientToUse = httpClient;
            policies.add(interceptorManager.getRecordPolicy());
            policies.add(new RetryPolicy());
        }

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClientToUse)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();

        return new ServiceBusManagementClientImplBuilder()
            .serializerAdapter(serializer)
            .endpoint(properties.getEndpoint().getHost())
            .apiVersion("2017-04")
            .pipeline(pipeline)
            .buildClient();
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
