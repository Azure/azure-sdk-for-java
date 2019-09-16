// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.TransportType;
import com.azure.core.credentials.TokenCredential;
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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static com.azure.messaging.eventhubs.implementation.CBSAuthorizationType.SHARED_ACCESS_SIGNATURE;

public class ReactorConnectionIntegrationTest extends IntegrationTestBase {
    @Mock
    private ManagementResponseMapper responseMapper;

    @Rule
    public TestName testName = new TestName();
    private ReactorConnection connection;

    public ReactorConnectionIntegrationTest() {
        super(new ClientLogger(ReactorConnectionIntegrationTest.class));
    }

    @Override
    protected String getTestName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        MockitoAnnotations.initMocks(this);

        ConnectionStringProperties connectionString = getConnectionStringProperties();

        TokenCredential tokenCredential = null;
        try {
            tokenCredential = new EventHubSharedAccessKeyCredential(connectionString.getSharedAccessKeyName(),
                connectionString.getSharedAccessKey(), ClientConstants.TOKEN_VALIDITY);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Assert.fail("Could not create tokenProvider :" + e);
        }

        final ConnectionOptions options = new ConnectionOptions(connectionString.getEndpoint().getHost(),
            connectionString.getEventHubName(), tokenCredential, SHARED_ACCESS_SIGNATURE, TransportType.AMQP,
            RETRY_OPTIONS, ProxyConfiguration.SYSTEM_DEFAULTS, Schedulers.elastic());

        AzureTokenManagerProvider tokenManagerProvider = new AzureTokenManagerProvider(options.getAuthorizationType(),
            options.getHost(), ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE);
        ReactorProvider reactorProvider = new ReactorProvider();
        ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(reactorProvider);
        connection = new ReactorConnection("test-connection-id", options, reactorProvider,
            handlerProvider, responseMapper, tokenManagerProvider);
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
        final AzureTokenManagerProvider provider = new AzureTokenManagerProvider(
            CBSAuthorizationType.SHARED_ACCESS_SIGNATURE,
            getConnectionStringProperties().getEndpoint().getHost(),
            ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE);

        final String tokenAudience = provider.getResourceString(getConnectionStringProperties().getEventHubName());

        // Act & Assert
        StepVerifier.create(connection.getCBSNode().flatMap(node -> node.authorize(tokenAudience)))
            .assertNext(expiration -> OffsetDateTime.now(ZoneOffset.UTC).isBefore(expiration))
            .verifyComplete();
    }
}
