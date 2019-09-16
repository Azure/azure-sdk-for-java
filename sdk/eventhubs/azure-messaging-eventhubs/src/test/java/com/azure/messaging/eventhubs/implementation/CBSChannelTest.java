// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubSharedAccessKeyCredential;
import com.azure.messaging.eventhubs.models.ProxyConfiguration;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;

import static com.azure.messaging.eventhubs.implementation.CBSAuthorizationType.SHARED_ACCESS_SIGNATURE;

public class CBSChannelTest extends IntegrationTestBase {
    private static final String CONNECTION_ID = "CbsChannelTest-Connection";

    @Mock
    private AmqpResponseMapper mapper;

    @Rule
    public TestName testName = new TestName();

    private AmqpConnection connection;
    private CBSChannel cbsChannel;
    private ConnectionStringProperties connectionString;
    private ReactorHandlerProvider handlerProvider;
    private TokenResourceProvider tokenResourceProvider;
    private ReactorProvider reactorProvider;

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
        tokenResourceProvider = new TokenResourceProvider(SHARED_ACCESS_SIGNATURE, connectionString.getEndpoint().getHost());

        TokenCredential tokenCredential = null;
        try {
            tokenCredential = new EventHubSharedAccessKeyCredential(connectionString.getSharedAccessKeyName(),
                connectionString.getSharedAccessKey(), ClientConstants.TOKEN_VALIDITY);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Assert.fail("Could not create tokenProvider :" + e);
        }

        final ConnectionOptions connectionOptions = new ConnectionOptions(connectionString.getEndpoint().getHost(),
            connectionString.getEventHubName(), tokenCredential, SHARED_ACCESS_SIGNATURE, TransportType.AMQP,
            RETRY_OPTIONS, ProxyConfiguration.SYSTEM_DEFAULTS, Schedulers.elastic());
        final RetryOptions retryOptions = new RetryOptions().setTryTimeout(Duration.ofMinutes(5));

        reactorProvider = new ReactorProvider();
        handlerProvider = new ReactorHandlerProvider(reactorProvider);
        connection = new ReactorConnection(CONNECTION_ID, connectionOptions, reactorProvider, handlerProvider, mapper);

        cbsChannel = new CBSChannel(connection, tokenCredential, connectionOptions.getAuthorizationType(),
            reactorProvider, handlerProvider, retryOptions);
    }

    @Override
    protected void afterTest() {
        mapper = null;

        if (cbsChannel != null) {
            cbsChannel.close();
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (IOException e) {
            Assert.fail("Could not close connection." + e.toString());
        }
    }

    @Test
    public void successfullyAuthorizes() {
        // Arrange
        final String tokenAudience = tokenResourceProvider.getResourceString(connectionString.getEventHubName());

        // Act & Assert
        StepVerifier.create(cbsChannel.authorize(tokenAudience))
            .assertNext(expiration -> OffsetDateTime.now().isBefore(expiration))
            .verifyComplete();
    }

    @Test
    public void unsuccessfulAuthorize() {
        // Arrange
        final String tokenAudience = tokenResourceProvider.getResourceString(connectionString.getEventHubName());
        final Duration duration = Duration.ofMinutes(10);

        TokenCredential tokenProvider = null;
        try {
            tokenProvider = new EventHubSharedAccessKeyCredential(connectionString.getSharedAccessKeyName(), "Invalid shared access key.", duration);
        } catch (Exception e) {
            Assert.fail("Could not create token provider: " + e.toString());
        }

        final CBSNode node = new CBSChannel(connection, tokenProvider, SHARED_ACCESS_SIGNATURE, reactorProvider,
            handlerProvider, new RetryOptions().setTryTimeout(Duration.ofMinutes(5)));

        // Act & Assert
        StepVerifier.create(node.authorize(tokenAudience))
            .expectErrorSatisfies(error -> {
                Assert.assertTrue(error instanceof AmqpException);

                AmqpException exception = (AmqpException) error;
                Assert.assertEquals(ErrorCondition.UNAUTHORIZED_ACCESS, exception.getErrorCondition());
                Assert.assertFalse(exception.isTransient());
                Assert.assertFalse(ImplUtils.isNullOrEmpty(exception.getMessage()));
            })
            .verify();
    }
}
