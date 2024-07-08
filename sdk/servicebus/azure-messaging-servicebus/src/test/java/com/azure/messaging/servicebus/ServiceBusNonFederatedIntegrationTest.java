// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.test.TestMode;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.NamespaceProperties;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.azure.messaging.servicebus.TestUtils.getConnectionString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Live tests with non-federated authentication.
 */
@Tag("integration")
@Execution(ExecutionMode.SAME_THREAD)
public class ServiceBusNonFederatedIntegrationTest extends IntegrationTestBase {
    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final String TEST_MESSAGE = "SSLorem ipsum dolor sit amet, consectetur adipiscing elit. Donec vehicula posuere lobortis. Aliquam finibus volutpat dolor, faucibus pellentesque ipsum bibendum vitae. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Ut sit amet urna hendrerit, dapibus justo a, sodales justo. Mauris finibus augue id pulvinar congue. Nam maximus luctus ipsum, at commodo ligula euismod ac. Phasellus vitae lacus sit amet diam porta placerat. \nUt sodales efficitur sapien ut posuere. Morbi sed tellus est. Proin eu erat purus. Proin massa nunc, condimentum id iaculis dignissim, consectetur et odio. Cras suscipit sem eu libero aliquam tincidunt. Nullam ut arcu suscipit, eleifend velit in, cursus libero. Ut eleifend facilisis odio sit amet feugiat. Phasellus at nunc sit amet elit sagittis commodo ac in nisi. Fusce vitae aliquam quam. Integer vel nibh euismod, tempus elit vitae, pharetra est. Duis vulputate enim a elementum dignissim. Morbi dictum enim id elit scelerisque, in elementum nulla pharetra. \nAenean aliquet aliquet condimentum. Proin dapibus dui id libero tempus feugiat. Sed commodo ligula a lectus mattis, vitae tincidunt velit auctor. Fusce quis semper dui. Phasellus eu efficitur sem. Ut non sem sit amet enim condimentum venenatis id dictum massa. Nullam sagittis lacus a neque sodales, et ultrices arcu mattis. Aliquam erat volutpat. \nAenean fringilla quam elit, id mattis purus vestibulum nec. Praesent porta eros in dapibus molestie. Vestibulum orci libero, tincidunt et turpis eget, condimentum lobortis enim. Fusce suscipit ante et mauris consequat cursus nec laoreet lorem. Maecenas in sollicitudin diam, non tincidunt purus. Nunc mauris purus, laoreet eget interdum vitae, placerat a sapien. In mi risus, blandit eu facilisis nec, molestie suscipit leo. Pellentesque molestie urna vitae dui faucibus bibendum. \nDonec quis ipsum ultricies, imperdiet ex vel, scelerisque eros. Ut at urna arcu. Vestibulum rutrum odio dolor, vitae cursus nunc pulvinar vel. Donec accumsan sapien in malesuada tempor. Maecenas in condimentum eros. Sed vestibulum facilisis massa a iaculis. Etiam et nibh felis. Donec maximus, sem quis vestibulum gravida, turpis risus congue dolor, pharetra tincidunt lectus nisi at velit.";

    ServiceBusNonFederatedIntegrationTest() {
        super(new ClientLogger(ServiceBusNonFederatedIntegrationTest.class));
    }

    @Test
    public void testBatchSendEventByAzureNameKeyCredential() {
        ConnectionStringProperties properties = new ConnectionStringProperties(TestUtils.getConnectionString(false));
        String fullyQualifiedNamespace = TestUtils.getFullyQualifiedDomainName(true);
        String sharedAccessKeyName = properties.getSharedAccessKeyName();
        String sharedAccessKey = properties.getSharedAccessKey();
        String queueName = getQueueName(TestUtils.USE_CASE_DEFAULT);

        final ServiceBusMessage testData = new ServiceBusMessage(TEST_MESSAGE.getBytes(UTF_8));

        ServiceBusSenderAsyncClient senderAsyncClient = toClose(new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, new AzureNamedKeyCredential(sharedAccessKeyName, sharedAccessKey))
            .sender()
            .queueName(queueName)
            .buildAsyncClient());
        StepVerifier.create(
                senderAsyncClient.createMessageBatch().flatMap(batch -> {
                    assertTrue(batch.tryAddMessage(testData));
                    return senderAsyncClient.sendMessages(batch);
                }))
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    public void testBatchSendEventByAzureSasCredential() {
        ConnectionStringProperties properties = new ConnectionStringProperties(TestUtils.getConnectionString(true));
        String fullyQualifiedNamespace = TestUtils.getFullyQualifiedDomainName(true);
        String sharedAccessSignature = properties.getSharedAccessSignature();
        String queueName = getQueueName(TestUtils.USE_CASE_DEFAULT);

        final ServiceBusMessage testData = new ServiceBusMessage(TEST_MESSAGE.getBytes(UTF_8));

        ServiceBusSenderAsyncClient senderAsyncClient = toClose(new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace,
                new AzureSasCredential(sharedAccessSignature))
            .sender()
            .queueName(queueName)
            .buildAsyncClient());

        StepVerifier.create(
                senderAsyncClient.createMessageBatch().flatMap(batch -> {
                    assertTrue(batch.tryAddMessage(testData));
                    return senderAsyncClient.sendMessages(batch);
                }))
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    void azureSasCredentialsTest() {
        final String fullyQualifiedDomainName = TestUtils.getFullyQualifiedDomainName(false);
        assumeTrue(!CoreUtils.isNullOrEmpty(fullyQualifiedDomainName), "FullyQualifiedDomainName is not set.");
        String connectionString = getConnectionString(true);
        Pattern sasPattern = Pattern.compile("SharedAccessSignature=(.*);?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = sasPattern.matcher(connectionString);
        assertTrue(matcher.find(), "Couldn't find SAS from connection string");

        ServiceBusAdministrationAsyncClient client = new ServiceBusAdministrationClientBuilder()
            .endpoint("https://" + fullyQualifiedDomainName)
            .credential(new AzureSasCredential(matcher.group(1)))
            .buildAsyncClient();

        StepVerifier.create(client.getNamespacePropertiesWithResponse())
            .assertNext(response -> {
                final NamespaceProperties np = response.getValue();
                assertNotNull(np.getName());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Override
    protected void assertRunnable() {
        final TestMode mode = super.getTestMode();
        if (mode == TestMode.PLAYBACK) {
            // AMQP traffic never gets recorded so there is no PLAYBACK supported.
            assumeTrue(false, "Skipping integration tests in playback mode.");
            return;
        }

        if (mode == TestMode.RECORD || mode == TestMode.LIVE) {
            // RECORD mode used in SDK-dev setup and LIVE mode on CI pipeline.
            if (!CoreUtils.isNullOrEmpty(TestUtils.getConnectionString(false))) {
                // non-federated integration tests are runnable using the connection string.
                return;
            }
        }
        assumeTrue(false, "Not running integration in record mode (missing authentication set up).");
    }
}
