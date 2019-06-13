// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

public class ReactorConnectionIntegrationTest extends ApiTestBase {
    private ReactorHandlerProvider handlerProvider;

    @Mock
    private AmqpResponseMapper responseMapper;

    @Rule
    public TestName testName = new TestName();
    private ReactorConnection connection;

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        skipIfNotRecordMode();

        MockitoAnnotations.initMocks(this);

        handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        connection = new ReactorConnection("test-connection-id", getConnectionParameters(),
            getReactorProvider(), handlerProvider, responseMapper);
    }

    @Override
    protected void afterTest() {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void getCbsNode() {
        // Act & Assert
        StepVerifier.create(connection.getCBSNode())
            .assertNext(node -> Assert.assertTrue(node instanceof CBSChannel))
            .verifyComplete();
    }

    @Test
    public void getCbsNodeAuthorize() {
        // Arrange
        final TokenResourceProvider provider = new TokenResourceProvider(CBSAuthorizationType.SHARED_ACCESS_SIGNATURE,
            getConnectionStringProperties().endpoint().getHost());

        final String tokenAudience = provider.getResourceString(getConnectionStringProperties().eventHubPath());

        // Act & Assert
        StepVerifier.create(connection.getCBSNode().flatMap(node -> node.authorize(tokenAudience)))
            .verifyComplete();
    }
}
