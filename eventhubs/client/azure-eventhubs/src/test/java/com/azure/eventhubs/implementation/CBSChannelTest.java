// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.eventhubs.CredentialInfo;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

public class CBSChannelTest extends ApiTestBase {
    private static final String CONNECTION_ID = "CbsChannelTest-Connection";

    @Mock
    private AmqpResponseMapper mapper;

    @Rule
    public TestName testName = new TestName();

    private AmqpConnection connection;
    private CBSChannel cbsChannel;
    private CredentialInfo credentials;
    private ReactorHandlerProvider handlerProvider;

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        MockitoAnnotations.initMocks(this);

        skipIfNotRecordMode();

        credentials = getCredentialInfo();

        handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        connection = new ReactorConnection(CONNECTION_ID, getConnectionParameters(), getReactorProvider(), handlerProvider, mapper);

        cbsChannel = new CBSChannel(connection, getTokenProvider(), getReactorProvider(), handlerProvider);
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
        final String tokenAudience = String.format(ClientConstants.TOKEN_AUDIENCE_FORMAT, credentials.endpoint().getHost(), credentials.eventHubPath());
        final Duration duration = Duration.ofMinutes(10);

        // Act & Assert
        StepVerifier.create(cbsChannel.authorize(tokenAudience, duration))
            .verifyComplete();
    }

    @Test
    public void unsuccessfulAuthorize() {
        // Arrange
        final String tokenAudience = String.format(ClientConstants.TOKEN_AUDIENCE_FORMAT, credentials.endpoint().getHost(), credentials.eventHubPath());
        final Duration duration = Duration.ofMinutes(10);

        TokenProvider tokenProvider = null;
        try {
            tokenProvider = new SharedAccessSignatureTokenProvider(credentials.sharedAccessKeyName(), "Invalid shared access key.");
        } catch (Exception e) {
            Assert.fail("Could not create token provider: " + e.toString());
        }

        final CBSNode node = new CBSChannel(connection, tokenProvider, getReactorProvider(), handlerProvider);

        // Act & Assert
        StepVerifier.create(node.authorize(tokenAudience, duration))
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
