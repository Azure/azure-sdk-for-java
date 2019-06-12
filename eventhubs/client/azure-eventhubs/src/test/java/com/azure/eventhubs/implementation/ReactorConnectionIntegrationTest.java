// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Locale;

import static org.mockito.Mockito.mock;

public class ReactorConnectionIntegrationTest extends ApiTestBase {
    private ReactorHandlerProvider handlerProvider;

    @Rule
    public TestName testName = new TestName();
    private ReactorConnection connection;

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        connection = new ReactorConnection("test-connection-id", getConnectionParameters(), getReactorProvider(), handlerProvider, mock(AmqpResponseMapper.class));
    }

    @Override
    protected void afterTest() {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void getCbsNode() {
        skipIfNotRecordMode();

        // Act & Assert
        StepVerifier.create(connection.getCBSNode())
            .assertNext(node -> Assert.assertTrue(node instanceof CBSChannel))
            .verifyComplete();
    }

    @Test
    public void getCbsNodeAuthorize() {
        skipIfNotRecordMode();

        // Arrange
        final String tokenAudience = String.format(Locale.US, ClientConstants.TOKEN_AUDIENCE_FORMAT,
            getCredentialInfo().endpoint().getHost(), getCredentialInfo().eventHubPath());
        final Duration tokenDuration = Duration.ofMinutes(5);

        // Act & Assert
        StepVerifier.create(connection.getCBSNode().flatMap(node -> node.authorize(tokenAudience, tokenDuration)))
            .verifyComplete();
    }
}
