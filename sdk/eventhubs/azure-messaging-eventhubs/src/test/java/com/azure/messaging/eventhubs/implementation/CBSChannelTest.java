// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.amqp.implementation.AzureTokenManagerProvider;
import com.azure.core.amqp.implementation.CBSChannel;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorConnection;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.RequestResponseChannel;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.models.ProxyConfiguration;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.GeneralUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubSharedAccessKeyCredential;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;

import static com.azure.core.amqp.implementation.CBSAuthorizationType.SHARED_ACCESS_SIGNATURE;

public class CBSChannelTest extends IntegrationTestBase {
    private static final String CONNECTION_ID = "CbsChannelTest-Connection";

    @Rule
    public TestName testName = new TestName();

    private TestReactorConnection connection;
    private CBSChannel cbsChannel;
    private ConnectionStringProperties connectionString;
    private AzureTokenManagerProvider azureTokenManagerProvider;
    @Mock
    private MessageSerializer messageSerializer;

    public CBSChannelTest() {
        super(new ClientLogger(CBSChannelTest.class));
    }

    @Override
    protected String getTestName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        MockitoAnnotations.initMocks(this);

        connectionString = getConnectionStringProperties();
        azureTokenManagerProvider = new AzureTokenManagerProvider(SHARED_ACCESS_SIGNATURE,
            connectionString.getEndpoint().getHost(), ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE);

        TokenCredential tokenCredential = null;
        try {
            tokenCredential = new EventHubSharedAccessKeyCredential(connectionString.getSharedAccessKeyName(),
                connectionString.getSharedAccessKey(), ClientConstants.TOKEN_VALIDITY);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Assert.fail("Could not create tokenProvider :" + e);
        }

        final ConnectionOptions connectionOptions = new ConnectionOptions(connectionString.getEndpoint().getHost(),
            connectionString.getEntityPath(), tokenCredential, SHARED_ACCESS_SIGNATURE, TransportType.AMQP,
            RETRY_OPTIONS, ProxyConfiguration.SYSTEM_DEFAULTS, Schedulers.elastic());
        final RetryOptions retryOptions = new RetryOptions().setTryTimeout(Duration.ofMinutes(5));

        ReactorProvider reactorProvider = new ReactorProvider();
        ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(reactorProvider);
        connection = new TestReactorConnection(CONNECTION_ID, connectionOptions, reactorProvider, handlerProvider,
            azureTokenManagerProvider, messageSerializer);

        final Mono<RequestResponseChannel> requestResponseChannel = connection.getCBSChannel();

        cbsChannel = new CBSChannel(requestResponseChannel, tokenCredential, connectionOptions.getAuthorizationType(),
            retryOptions);
    }

    @Override
    protected void afterTest() {
        if (cbsChannel != null) {
            cbsChannel.close();
        }

        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void successfullyAuthorizes() {
        // Arrange
        final String tokenAudience = azureTokenManagerProvider.getResourceString(connectionString.getEntityPath());

        // Act & Assert
        StepVerifier.create(cbsChannel.authorize(tokenAudience))
            .assertNext(expiration -> OffsetDateTime.now().isBefore(expiration))
            .verifyComplete();
    }

    @Test
    public void unsuccessfulAuthorize() {
        // Arrange
        final String tokenAudience = azureTokenManagerProvider.getResourceString(connectionString.getEntityPath());
        final Duration duration = Duration.ofMinutes(10);

        TokenCredential tokenProvider = null;
        try {
            tokenProvider = new EventHubSharedAccessKeyCredential(connectionString.getSharedAccessKeyName(), "Invalid shared access key.", duration);
        } catch (Exception e) {
            Assert.fail("Could not create token provider: " + e.toString());
        }

        final Mono<RequestResponseChannel> requestResponseChannel = connection.getCBSChannel();

        final CBSNode node = new CBSChannel(requestResponseChannel, tokenProvider, SHARED_ACCESS_SIGNATURE,
            new RetryOptions().setTryTimeout(Duration.ofMinutes(5)));

        // Act & Assert
        StepVerifier.create(node.authorize(tokenAudience))
            .expectErrorSatisfies(error -> {
                Assert.assertTrue(error instanceof AmqpException);

                AmqpException exception = (AmqpException) error;
                Assert.assertEquals(ErrorCondition.UNAUTHORIZED_ACCESS, exception.getErrorCondition());
                Assert.assertFalse(exception.isTransient());
                Assert.assertFalse(GeneralUtils.isNullOrEmpty(exception.getMessage()));
            })
            .verify();
    }

    private static final class TestReactorConnection extends ReactorConnection {
        private TestReactorConnection(String connectionId, ConnectionOptions connectionOptions,
                                      ReactorProvider reactorProvider, ReactorHandlerProvider handlerProvider,
                                      TokenManagerProvider tokenManagerProvider, MessageSerializer messageSerializer) {
            super(connectionId, connectionOptions, reactorProvider, handlerProvider, tokenManagerProvider,
                messageSerializer);
        }

        private Mono<RequestResponseChannel> getCBSChannel() {
            return createRequestResponseChannel("cbs-session", "cbs", "$cbs");
        }
    }
}
