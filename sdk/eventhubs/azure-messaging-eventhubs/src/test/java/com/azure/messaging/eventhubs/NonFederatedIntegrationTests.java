package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.experimental.util.tracing.LoggingTracerProvider;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.logging.ClientLogger;
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
    private static final ClientOptions OPTIONS_WITH_TRACING = new ClientOptions().setTracingOptions(new LoggingTracerProvider.LoggingTracingOptions());
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

        ConnectionStringProperties properties = TestUtils.getConnectionStringProperties(true);
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
}
