// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusServiceVersion;
import com.azure.messaging.servicebus.TestUtils;
import com.azure.messaging.servicebus.administration.ServiceBusSupplementaryAuthHeaderPolicy;
import com.azure.messaging.servicebus.administration.implementation.EntitiesImpl;
import com.azure.messaging.servicebus.administration.implementation.EntityHelper;
import com.azure.messaging.servicebus.administration.implementation.ServiceBusManagementClientImpl;
import com.azure.messaging.servicebus.administration.implementation.ServiceBusManagementSerializer;
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.administration.implementation.models.CreateQueueBodyContent;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionEntry;
import com.azure.messaging.servicebus.administration.implementation.models.QueueDescriptionFeed;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for {@link ServiceBusManagementClientImpl}.
 */
class ServiceBusAdministrationClientImplIntegrationTests extends TestProxyTestBase {
    private static final ClientLogger LOGGER
        = new ClientLogger(ServiceBusAdministrationClientImplIntegrationTests.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final ServiceBusManagementSerializer SERIALIZER = new ServiceBusManagementSerializer();

    private final Duration timeout = Duration.ofSeconds(30);
    private final AtomicReference<TokenCredential> credentialCached = new AtomicReference<>();

    /**
     * Verifies we can get queue information.
     */
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    void getQueueImplementation(HttpClient httpClient) {
        // Arrange
        final ServiceBusManagementClientImpl managementClient = createClient(httpClient);
        final EntitiesImpl entityClient = managementClient.getEntities();
        final String queueName = TestUtils.getEntityName(TestUtils.getQueueBaseName(), 0);

        // Act & Assert
        StepVerifier.create(entityClient.getWithResponseAsync(queueName, true, Context.NONE)).assertNext(response -> {
            final QueueDescriptionEntry deserialize = deserialize(response, QueueDescriptionEntry.class);
            assertNotNull(deserialize);
            assertNotNull(deserialize.getContent());

            final QueueDescription properties = deserialize.getContent().getQueueDescription();
            assertNotNull(properties);
            assertFalse(properties.getLockDuration().isZero());
        }).expectComplete().verify(DEFAULT_TIMEOUT);
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
        final CreateQueueOptions options = new CreateQueueOptions().setMaxDeliveryCount(15);
        final QueueDescription queueProperties = EntityHelper.getQueueDescription(options);
        final CreateQueueBody createEntity = new CreateQueueBody();
        final CreateQueueBodyContent content
            = new CreateQueueBodyContent().setType("application/xml").setQueueDescription(queueProperties);
        createEntity.setContent(content);

        LOGGER.info("Creating queue: {}", queueName);

        // Act & Assert
        StepVerifier.create(entityClient.putWithResponseAsync(queueName, createEntity, null, Context.NONE))
            .assertNext(response -> {
                QueueDescriptionEntry entry = deserialize(response, QueueDescriptionEntry.class);

                assertNotNull(entry);
                assertNotNull(entry.getContent().getQueueDescription());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
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
        final CreateQueueOptions description = new CreateQueueOptions().setMaxDeliveryCount(15);
        final QueueDescription queueProperties = EntityHelper.getQueueDescription(description);
        final CreateQueueBody createEntity = new CreateQueueBody();
        final CreateQueueBodyContent content
            = new CreateQueueBodyContent().setType("application/xml").setQueueDescription(queueProperties);
        createEntity.setContent(content);

        LOGGER.info("Creating queue: {}", queueName);

        // This is not part of the scenario. We'll ensure it is created.
        Response<Object> response
            = entityClient.putWithResponseAsync(queueName, createEntity, null, Context.NONE).block(timeout);
        assertNotNull(response);

        // Act & Assert
        StepVerifier.create(entityClient.deleteWithResponseAsync(queueName, Context.NONE))
            .assertNext(deletedResponse -> assertEquals(200, deletedResponse.getStatusCode()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
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

        final String queueName = TestUtils.getEntityName(TestUtils.getQueueBaseName(), 5);
        final Response<Object> response
            = entityClient.getWithResponseAsync(queueName, true, Context.NONE).block(Duration.ofSeconds(30));
        assertNotNull(response);
        final QueueDescriptionEntry deserialize = deserialize(response, QueueDescriptionEntry.class);
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

        CreateQueueBody updated = new CreateQueueBody()
            .setContent(new CreateQueueBodyContent().setQueueDescription(properties).setType("application/xml"));

        // Act & Assert
        StepVerifier.create(entityClient.putWithResponseAsync(queueName, updated, "*", Context.NONE))
            .assertNext(update -> {
                final QueueDescriptionEntry updatedProperties = deserialize(update, QueueDescriptionEntry.class);
                assertNotNull(updatedProperties);
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
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
                QueueDescriptionFeed deserialize = deserialize(response, QueueDescriptionFeed.class);

                assertNotNull(deserialize);
                assertNotNull(deserialize.getEntry());
                assertTrue(deserialize.getEntry().size() > 2);
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    private ServiceBusManagementClientImpl createClient(HttpClient httpClient) {
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy());
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        final TokenCredential tokenCredential;
        final HttpClient httpClientToUse;
        final String fullyQualifiedNamespace;
        if (interceptorManager.isPlaybackMode()) {
            fullyQualifiedNamespace = TestUtils.getFullyQualifiedDomainName(true);

            httpClientToUse = interceptorManager.getPlaybackClient();
            tokenCredential = new MockTokenCredential();
        } else if (interceptorManager.isLiveMode()) {
            fullyQualifiedNamespace = TestUtils.getFullyQualifiedDomainName(false);
            assumeTrue(!CoreUtils.isNullOrEmpty(fullyQualifiedNamespace), "FullyQualifiedDomainName is not set.");

            httpClientToUse = httpClient;
            tokenCredential = TestUtils.getPipelineCredential(credentialCached);
        } else if (interceptorManager.isRecordMode()) {
            // Record Mode.
            final String connectionString = TestUtils.getConnectionString(false);
            if (CoreUtils.isNullOrEmpty(connectionString)) {
                fullyQualifiedNamespace = TestUtils.getFullyQualifiedDomainName(false);
                assumeTrue(!CoreUtils.isNullOrEmpty(fullyQualifiedNamespace), "fullyQualifiedNamespace is not set.");

                tokenCredential = new DefaultAzureCredentialBuilder().build();
            } else {
                tokenCredential = new ServiceBusSharedKeyCredential(connectionString);

                ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
                fullyQualifiedNamespace = properties.getEndpoint().getHost();
            }

            httpClientToUse = httpClient;
            policies.add(interceptorManager.getRecordPolicy());
        } else {
            throw new UnsupportedOperationException("Test mode is not supported: " + getTestMode());
        }

        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(TestUtils.TEST_PROXY_SANITIZERS);
            interceptorManager.addMatchers(TestUtils.TEST_PROXY_REQUEST_MATCHERS);
        }

        policies.add(new ServiceBusTokenCredentialHttpPolicy(tokenCredential));
        policies.add(new AddHeadersFromContextPolicy());
        policies.add(new ServiceBusSupplementaryAuthHeaderPolicy(tokenCredential));

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClientToUse)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();

        return new ServiceBusManagementClientImpl(pipeline, SERIALIZER, fullyQualifiedNamespace,
            ServiceBusServiceVersion.getLatest().getVersion());
    }

    private <T> T deserialize(Response<Object> response, Class<T> clazz) {
        final Object body = response.getValue();
        final String contents = String.valueOf(body);
        final T deserialize;
        try {
            deserialize = SERIALIZER.deserialize(contents, clazz);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(
                String.format("Exception while deserializing. Body: [%s]. Class: %s", contents, clazz), e));
        }

        if (deserialize == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("'deserialize' should not be null. Body: [%s]. Class: [%s]", contents, clazz)));
        }

        return deserialize;
    }
}
