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
import com.azure.core.util.logging.ClientLogger;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Synchronous access to the project's telemetry configuration.
 * Instances are created through {@link AIProjectClientBuilder#buildBetaTelemetryClient()}.
 */
@ServiceClient(builder = AIProjectClientBuilder.class)
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class BetaTelemetryClient {
    private static final ClientLogger LOGGER = new ClientLogger(BetaTelemetryClient.class);
    private final ConnectionsClient connections;
    private final AtomicReference<String> connectionString = new AtomicReference<>();

    BetaTelemetryClient(ConnectionsClient connections) {
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
    public String getApplicationInsightsConnectionString() {
        String cached = connectionString.get();
        if (cached != null) {
            return cached;
        }
        Iterator<Connection> iterator
            = connections.listConnections(ConnectionType.APPLICATION_INSIGHTS, null).iterator();
        if (!iterator.hasNext()) {
            throw LOGGER
                .logExceptionAsError(new ResourceNotFoundException("No Application Insights connection found.", null));
        }
        String name = iterator.next().getName();
        if (CoreUtils.isNullOrEmpty(name)) {
            throw LOGGER
                .logExceptionAsError(new ResourceNotFoundException("No Application Insights connection found.", null));
        }
        Connection connection = connections.getConnection(name, true);
        if (!(connection.getCredential() instanceof ApiKeyCredential)) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Application Insights connection does not use API Key credentials."));
        }
        String value = ((ApiKeyCredential) connection.getCredential()).getApiKey();
        if (CoreUtils.isNullOrEmpty(value)) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Application Insights connection does not have a connection string."));
        }
        connectionString.set(value);
        return value;
    }
}
