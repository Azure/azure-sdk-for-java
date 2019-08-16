// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubSharedAccessKeyCredential;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;

public class CBSChannelTest extends ApiTestBase {
    private static final String CONNECTION_ID = "CbsChannelTest-Connection";

    @Mock
    private AmqpResponseMapper mapper;

    @Rule
    public TestName testName = new TestName();

    private AmqpConnection connection;
    private CBSChannel cbsChannel;
    private ConnectionStringProperties credentials;
    private ReactorHandlerProvider handlerProvider;
    private TokenResourceProvider tokenResourceProvider;

    public CBSChannelTest() {
        super(new ClientLogger(CBSChannelTest.class));
    }

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        MockitoAnnotations.initMocks(this);

        credentials = getConnectionStringProperties();
        tokenResourceProvider = new TokenResourceProvider(CBSAuthorizationType.SHARED_ACCESS_SIGNATURE, credentials.endpoint().getHost());

        handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        connection = new ReactorConnection(CONNECTION_ID, getConnectionOptions(), getReactorProvider(),
            handlerProvider, mapper);

        cbsChannel = new CBSChannel(connection, getTokenCredential(), getAuthorizationType(), getReactorProvider(),
            handlerProvider, new RetryOptions().tryTimeout(Duration.ofMinutes(5)));
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
        final String tokenAudience = tokenResourceProvider.getResourceString(credentials.eventHubName());

        // Act & Assert
        StepVerifier.create(cbsChannel.authorize(tokenAudience))
            .assertNext(expiration -> OffsetDateTime.now().isBefore(expiration))
            .verifyComplete();
    }

    @Test
    public void unsuccessfulAuthorize() {
        skipIfNotRecordMode();

        // Arrange
        final String tokenAudience = tokenResourceProvider.getResourceString(credentials.eventHubName());
        final Duration duration = Duration.ofMinutes(10);

        TokenCredential tokenProvider = null;
        try {
            tokenProvider = new EventHubSharedAccessKeyCredential(credentials.sharedAccessKeyName(), "Invalid shared access key.", duration);
        } catch (Exception e) {
            Assert.fail("Could not create token provider: " + e.toString());
        }

        final CBSNode node = new CBSChannel(connection, tokenProvider, getAuthorizationType(), getReactorProvider(),
            handlerProvider, new RetryOptions().tryTimeout(Duration.ofMinutes(5)));

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
