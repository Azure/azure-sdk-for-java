// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.AzureTokenManagerProvider;
import com.azure.core.amqp.implementation.CbsAuthorizationType;
import com.azure.core.amqp.implementation.ClaimsBasedSecurityChannel;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorConnection;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.IntegrationTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static com.azure.core.amqp.implementation.CbsAuthorizationType.SHARED_ACCESS_SIGNATURE;

public class ReactorConnectionIntegrationTest extends IntegrationTestBase {

    private ReactorConnection connection;

    @Mock
    private MessageSerializer serializer;

    public ReactorConnectionIntegrationTest() {
        super(new ClientLogger(ReactorConnectionIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        MockitoAnnotations.initMocks(this);

        ConnectionStringProperties connectionString = getConnectionStringProperties();

        TokenCredential tokenCredential = new EventHubSharedKeyCredential(connectionString.getSharedAccessKeyName(),
            connectionString.getSharedAccessKey());

        final ConnectionOptions options = new ConnectionOptions(connectionString.getEndpoint().getHost(),
            connectionString.getEntityPath(), tokenCredential, SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP,
            RETRY_OPTIONS, ProxyOptions.SYSTEM_DEFAULTS, Schedulers.single());

        AzureTokenManagerProvider tokenManagerProvider = new AzureTokenManagerProvider(options.getAuthorizationType(),
            options.getFullyQualifiedNamespace(), ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE);
        ReactorProvider reactorProvider = new ReactorProvider();
        ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(reactorProvider);
        connection = new ReactorConnection("test-connection-id", options, reactorProvider,
            handlerProvider, tokenManagerProvider, serializer);
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
        StepVerifier.create(connection.getClaimsBasedSecurityNode())
            .assertNext(node -> Assertions.assertTrue(node instanceof ClaimsBasedSecurityChannel))
            .verifyComplete();
    }

    @Test
    public void getCbsNodeAuthorize() {
        // Arrange
        final AzureTokenManagerProvider provider = new AzureTokenManagerProvider(
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE,
            getConnectionStringProperties().getEndpoint().getHost(),
            ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE);

        final String tokenAudience = provider.getResourceString(getConnectionStringProperties().getEntityPath());

        // Act & Assert
        StepVerifier.create(connection.getClaimsBasedSecurityNode().flatMap(node -> node.authorize(tokenAudience, tokenAudience)))
            .assertNext(expiration -> OffsetDateTime.now(ZoneOffset.UTC).isBefore(expiration))
            .verifyComplete();
    }
}
