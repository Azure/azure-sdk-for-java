// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.models.ApiKeyCredential;
import com.azure.ai.projects.models.Connection;
import com.azure.ai.projects.models.ConnectionType;
import com.azure.ai.projects.implementation.utils.Beta;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.annotation.ReturnType;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Mono;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Asynchronous access to the project's telemetry configuration.
 * Instances are created through
 * {@link AIProjectClientBuilder.BetaAIProjectClientBuilder#buildBetaTelemetryAsyncClient()}.
 */
@ServiceClient(builder = AIProjectClientBuilder.class, isAsync = true)
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class BetaTelemetryAsyncClient {
    private final ConnectionsAsyncClient connections;
    private final AtomicReference<String> connectionString = new AtomicReference<>();

    BetaTelemetryAsyncClient(ConnectionsAsyncClient connections) {
        this.connections = connections;
    }

    /**
     * Gets the project's Application Insights connection string, caching successful lookups for this client.
     *
     * @return the Application Insights connection string.
     * @throws ResourceNotFoundException if the project has no Application Insights connection.
     * @throws IllegalStateException if the connection does not contain a nonempty API key credential.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getApplicationInsightsConnectionString() {
        return Mono.defer(() -> {
            String cached = connectionString.get();
            if (cached != null) {
                return Mono.just(cached);
            }
            return connections.listConnections(ConnectionType.APPLICATION_INSIGHTS, null)
                .next()
                .filter(connection -> !CoreUtils.isNullOrEmpty(connection.getName()))
                .switchIfEmpty(
                    Mono.error(new ResourceNotFoundException("No Application Insights connection found.", null)))
                .flatMap(connection -> connections.getConnection(connection.getName(), true))
                .map(BetaTelemetryAsyncClient::getConnectionString)
                .doOnNext(connectionString::set);
        });
    }

    private static String getConnectionString(Connection connection) {
        if (!(connection.getCredential() instanceof ApiKeyCredential)) {
            throw new IllegalStateException("Application Insights connection does not use API Key credentials.");
        }
        String value = ((ApiKeyCredential) connection.getCredential()).getApiKey();
        if (CoreUtils.isNullOrEmpty(value)) {
            throw new IllegalStateException("Application Insights connection does not have a connection string.");
        }
        return value;
    }
}
