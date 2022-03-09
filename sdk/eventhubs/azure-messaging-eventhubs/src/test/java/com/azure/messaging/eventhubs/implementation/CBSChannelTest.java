// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AzureTokenManagerProvider;
import com.azure.core.amqp.implementation.ClaimsBasedSecurityChannel;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorConnection;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.RequestResponseChannel;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.Header;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.IntegrationTestBase;
import com.azure.messaging.eventhubs.TestUtils;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.SslDomain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 * Verifies we authorize with Event Hubs CBS node correctly.
 */
@Tag(TestUtils.INTEGRATION)
class CBSChannelTest extends IntegrationTestBase {
    private static final String CONNECTION_ID = "CbsChannelTest-Connection";
    private static String product;
    private static String clientVersion;

    @Mock
    private MessageSerializer messageSerializer;

    private final ClientOptions clientOptions = new ClientOptions();

    private TestReactorConnection connection;
    private ClaimsBasedSecurityChannel cbsChannel;
    private ConnectionStringProperties connectionProperties;
    private AzureTokenManagerProvider azureTokenManagerProvider;
    private AmqpRetryOptions retryOptions;
    private ReactorProvider reactorProvider;
    private ReactorHandlerProvider handlerProvider;
    private String tokenAudience;

    CBSChannelTest() {
        super(new ClientLogger(CBSChannelTest.class));
    }

    @BeforeAll
    static void init() {
        Map<String, String> properties = CoreUtils.getProperties("azure-messaging-eventhubs.properties");
        product = properties.get("name");
        clientVersion = properties.get("version");
    }

    @Override
    protected void beforeTest() {
        MockitoAnnotations.initMocks(this);

        connectionProperties = getConnectionStringProperties();
        azureTokenManagerProvider = new AzureTokenManagerProvider(CbsAuthorizationType.SHARED_ACCESS_SIGNATURE,
            connectionProperties.getEndpoint().getHost(), ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE);
        tokenAudience = azureTokenManagerProvider.getScopesFromResource(connectionProperties.getEntityPath());

        retryOptions = new AmqpRetryOptions().setTryTimeout(Duration.ofMinutes(1));
        reactorProvider = new ReactorProvider();
        handlerProvider = new ReactorHandlerProvider(reactorProvider);

        clientOptions.setHeaders(
            Arrays.asList(new Header("name", product), new Header("version", clientVersion)));
    }

    @Override
    protected void afterTest() {
        if (cbsChannel != null) {
            cbsChannel.close();
        }

        if (connection != null) {
            connection.dispose();
        }
    }

    @Test
    void successfullyAuthorizes() {
        // Arrange
        TokenCredential tokenCredential = new EventHubSharedKeyCredential(
            connectionProperties.getSharedAccessKeyName(), connectionProperties.getSharedAccessKey());
        ConnectionOptions connectionOptions = new ConnectionOptions(connectionProperties.getEndpoint().getHost(),
            tokenCredential, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE,
            AmqpTransportType.AMQP, RETRY_OPTIONS, ProxyOptions.SYSTEM_DEFAULTS, Schedulers.elastic(), clientOptions,
            SslDomain.VerifyMode.VERIFY_PEER_NAME, "test-product", "test-client-version");
        connection = new TestReactorConnection(CONNECTION_ID, connectionOptions, reactorProvider, handlerProvider,
            azureTokenManagerProvider, messageSerializer);

        final Mono<RequestResponseChannel> requestResponseChannel = connection.getCBSChannel("valid-cbs");
        cbsChannel = new ClaimsBasedSecurityChannel(requestResponseChannel, tokenCredential,
            connectionOptions.getAuthorizationType(), retryOptions);

        // Act & Assert
        StepVerifier.create(cbsChannel.authorize(tokenAudience, tokenAudience))
            .assertNext(expiration -> OffsetDateTime.now().isBefore(expiration))
            .verifyComplete();
    }

    @Test
    void unsuccessfulAuthorize() {
        // Arrange
        final TokenCredential invalidToken = new EventHubSharedKeyCredential(
            connectionProperties.getSharedAccessKeyName(), "Invalid shared access key.");

        final ConnectionOptions connectionOptions = new ConnectionOptions(connectionProperties.getEndpoint().getHost(),
            invalidToken, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE,
            AmqpTransportType.AMQP, RETRY_OPTIONS, ProxyOptions.SYSTEM_DEFAULTS, Schedulers.elastic(), clientOptions,
            SslDomain.VerifyMode.VERIFY_PEER, "test-product", "test-client-version");
        connection = new TestReactorConnection(CONNECTION_ID, connectionOptions, reactorProvider, handlerProvider,
            azureTokenManagerProvider, messageSerializer);

        final Mono<RequestResponseChannel> requestResponseChannel = connection.getCBSChannel("invalid-sas");
        cbsChannel = new ClaimsBasedSecurityChannel(requestResponseChannel, invalidToken,
            connectionOptions.getAuthorizationType(), retryOptions);

        // Act & Assert
        StepVerifier.create(cbsChannel.authorize(tokenAudience, tokenAudience))
            .expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof AmqpException);

                AmqpException exception = (AmqpException) error;
                Assertions.assertEquals(AmqpErrorCondition.UNAUTHORIZED_ACCESS, exception.getErrorCondition());
                Assertions.assertFalse(exception.isTransient());
                Assertions.assertFalse(CoreUtils.isNullOrEmpty(exception.getMessage()));
            })
            .verify();
    }

    private static final class TestReactorConnection extends ReactorConnection {

        private TestReactorConnection(String connectionId, ConnectionOptions connectionOptions,
            ReactorProvider reactorProvider, ReactorHandlerProvider handlerProvider,
            TokenManagerProvider tokenManagerProvider, MessageSerializer messageSerializer) {
            super(connectionId, connectionOptions, reactorProvider, handlerProvider, tokenManagerProvider,
                messageSerializer, SenderSettleMode.SETTLED, ReceiverSettleMode.SECOND);
        }

        private Mono<RequestResponseChannel> getCBSChannel(String linkName) {
            final String sessionName = "cbs-" + linkName;
            return createRequestResponseChannel(sessionName, linkName, "$cbs");
        }
    }
}
