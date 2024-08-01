// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.EventHubSharedKeyCredential;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import reactor.test.StepVerifier;

import static com.azure.messaging.eventhubs.TestUtils.getEventHubName;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NonFederatedIntegrationTests extends IntegrationTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(NonFederatedIntegrationTests.class);
    private static final String PARTITION_ID = "2";

    NonFederatedIntegrationTests() {
        super(LOGGER);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_EVENTHUBS_CONNECTION_STRING_WITH_SAS", matches =
        ".*ShadAccessSignature .*")
    void sendWithSasConnectionString() {
        final String eventHubName = TestUtils.getEventHubName();
        final EventData event = new EventData("body");
        final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);

        final EventHubProducerAsyncClient eventHubAsyncClient = toClose(new EventHubClientBuilder()
            .connectionString(TestUtils.getConnectionString(true))
            .eventHubName(eventHubName)
            .buildAsyncProducerClient());

        StepVerifier.create(eventHubAsyncClient.getEventHubProperties())
            .assertNext(properties -> {
                Assertions.assertEquals(getEventHubName(), properties.getName());
                Assertions.assertEquals(NUMBER_OF_PARTITIONS, properties.getPartitionIds().stream().count());
            })
            .expectComplete()
            .verify(TIMEOUT);

        StepVerifier.create(eventHubAsyncClient.send(event, options))
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    public void sendAndReceiveEventByAzureSasCredential() {
        Assumptions.assumeTrue(TestUtils.getConnectionString(true) != null,
            "SAS was not set. Can't run test scenario.");

        ConnectionStringProperties properties = getConnectionStringProperties(true);
        String fullyQualifiedNamespace = properties.getEndpoint().getHost();
        String sharedAccessSignature = properties.getSharedAccessSignature();
        String eventHubName = properties.getEntityPath();

        final EventData testData = new EventData("test-contents".getBytes(UTF_8));

        EventHubProducerAsyncClient asyncProducerClient = toClose(new EventHubClientBuilder()
            .credential(fullyQualifiedNamespace, eventHubName,
                new AzureSasCredential(sharedAccessSignature))
            .buildAsyncProducerClient());

        StepVerifier.create(asyncProducerClient.createBatch().flatMap(batch -> {
            assertTrue(batch.tryAdd(testData));
            return asyncProducerClient.send(batch);
        }))
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    public void sendAndReceiveEventByAzureNameKeyCredential() {
        ConnectionStringProperties properties = getConnectionStringProperties();
        String fullyQualifiedNamespace = properties.getEndpoint().getHost();
        String sharedAccessKeyName = properties.getSharedAccessKeyName();
        String sharedAccessKey = properties.getSharedAccessKey();
        String eventHubName = TestUtils.getEventHubName();

        final EventData testData = new EventData("items".getBytes(UTF_8));

        EventHubProducerAsyncClient asyncProducerClient = toClose(new EventHubClientBuilder()
            .credential(fullyQualifiedNamespace, eventHubName,
                new AzureNamedKeyCredential(sharedAccessKeyName, sharedAccessKey))
            .buildAsyncProducerClient());

        StepVerifier.create(asyncProducerClient.createBatch().flatMap(batch -> {
                assertTrue(batch.tryAdd(testData));
                return asyncProducerClient.send(batch);
            }))
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that error conditions are handled for fetching Event Hub metadata.
     */
    @Test
    public void getPartitionPropertiesInvalidToken() {
        // Arrange
        final ConnectionStringProperties original = getConnectionStringProperties();
        final TokenCredential invalidTokenCredential = new EventHubSharedKeyCredential(
            original.getSharedAccessKeyName(), "invalid-sas-key-value", TIMEOUT);
        final String eventHubName = TestUtils.getEventHubName();

        // Act & Assert
        try (EventHubAsyncClient invalidClient = createBuilder()
            .credential(original.getEndpoint().getHost(), eventHubName, invalidTokenCredential)
            .buildAsyncClient()) {
            StepVerifier.create(invalidClient.getProperties())
                .expectErrorSatisfies(error -> {
                    Assertions.assertTrue(error instanceof AmqpException);

                    AmqpException exception = (AmqpException) error;
                    Assertions.assertEquals(AmqpErrorCondition.UNAUTHORIZED_ACCESS, exception.getErrorCondition());
                    Assertions.assertFalse(exception.isTransient());
                    Assertions.assertFalse(CoreUtils.isNullOrEmpty(exception.getMessage()));
                })
                .verify(TIMEOUT);
        }
    }

    /**
     * Verifies that error conditions are handled for fetching partition metadata.
     */
    @Test
    public void getPartitionPropertiesNonExistentHub() {
        // Arrange
        final ConnectionStringProperties original = getConnectionStringProperties();
        final TokenCredential validCredentials = new EventHubSharedKeyCredential(
            original.getSharedAccessKeyName(), original.getSharedAccessKey(), TIMEOUT);

        // Act & Assert
        try (EventHubAsyncClient invalidClient = createBuilder()
            .credential(original.getEndpoint().getHost(), "does-not-exist", validCredentials)
            .buildAsyncClient()) {
            StepVerifier.create(invalidClient.getPartitionIds())
                .expectErrorSatisfies(error -> {
                    Assertions.assertTrue(error instanceof AmqpException);

                    AmqpException exception = (AmqpException) error;
                    Assertions.assertEquals(AmqpErrorCondition.NOT_FOUND, exception.getErrorCondition());
                    Assertions.assertFalse(exception.isTransient());
                    Assertions.assertFalse(CoreUtils.isNullOrEmpty(exception.getMessage()));
                })
                .verify(TIMEOUT);
        }
    }

    private static ConnectionStringProperties getConnectionStringProperties() {
        return new ConnectionStringProperties(TestUtils.getConnectionString(false));
    }

    private static ConnectionStringProperties getConnectionStringProperties(boolean withSas) {
        return new ConnectionStringProperties(TestUtils.getConnectionString(withSas));
    }
}
