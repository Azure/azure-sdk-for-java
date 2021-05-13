// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.credential.TokenCredential;

import java.net.URI;

/**
 * A utility class that parses a connection string into sections. An Event Hubs connection string is a set of key-value
 * pairs separated by semi-colon. A typical example is
 * {@code "Endpoint=sb://foo.EventHub.windows.net/;SharedAccessKeyName=someKeyName;SharedAccessKey=someKeyValue"}.
 *
 * <p>
 * A connection may have the following sections:
 * <ul>
 *     <li>Endpoint, which is mandatory. The hostname part of it is the "Fully qualified namespace".</li>
 *     <li>SharedAccessKeyName and SharedAccessKey, optional, used to authenticate the access to the EventHub.</li>
 *     <li>SharedAccessSignature, optional, an alternative way to authenticate the access to the EventHub.</li>
 *     <li>EntityPath, optional, the queue name or the topic name under the service namespace</li>
 * </ul>
 *
 * <p>
 * When you have an Event Hubs connection string, you can use {@link EventHubClientBuilder#connectionString(String)}
 * to build a client. If you'd like to use a {@link TokenCredential} to access an Event Hub, you can use this utility
 * class to get the fully qualified namespace and entity path from the connection
 * string and then use {@link EventHubClientBuilder#credential(String, String, TokenCredential)}.
 * </p>
 *
 * @see EventHubClientBuilder#connectionString(String, String)
 */
public final class EventHubConnectionStringProperties {
    private final URI endpoint;
    private final String entityPath;
    private final String sharedAccessKeyName;
    private final String sharedAccessKey;

    private EventHubConnectionStringProperties(ConnectionStringProperties properties) {
        this.endpoint = properties.getEndpoint();
        this.entityPath = properties.getEntityPath();
        this.sharedAccessKeyName = properties.getSharedAccessKeyName();
        this.sharedAccessKey = properties.getSharedAccessKey();
    }
    /**
     * Parse a Event Hub connection string into an instance of this class.
     *
     * @param connectionString The connection string to be parsed.
     *
     * @return An instance of this class.
     * @throws NullPointerException if {@code connectionString} is null.
     * @throws IllegalArgumentException if the {@code connectionString} is empty or malformed.
     */
    public static EventHubConnectionStringProperties parse(String connectionString) {
        return new EventHubConnectionStringProperties(new ConnectionStringProperties(connectionString));
    }

    /**
     * Gets the "EntityPath" value of the connection string.
     *
     * @return The entity path, or {@code null} if the connection string doesn't have an "EntityPath".
     */
    public String getEntityPath() {
        return entityPath;
    }

    /**
     * Gets the "Endpoint" value of the connection string.
     *
     * @return The endpoint.
     */
    public String getEndpoint() {
        return String.format("%s://%s", endpoint.getScheme(), endpoint.getHost());
    }

    /**
     * Gets the fully qualified namespace, or hostname, from the connection string "Endpoint" section.
     *
     * @return The fully qualified namespace.
     */
    public String getFullyQualifiedNamespace() {
        return endpoint.getHost();
    }

    /**
     * Gets the "SharedAccessKeyName" section of the connection string.
     *
     * @return The shared access key name, or {@code null} if the connection string doesn't have a
     *     "SharedAccessKeyName".
     */
    public String getSharedAccessKeyName() {
        return sharedAccessKeyName;
    }

    /**
     * Gets the "SharedAccessSignature" section of the connection string.
     *
     * @return The shared access key value, or {@code null} if the connection string doesn't have a
     *     "SharedAccessSignature".
     */
    public String getSharedAccessKey() {
        return sharedAccessKey;
    }

}
