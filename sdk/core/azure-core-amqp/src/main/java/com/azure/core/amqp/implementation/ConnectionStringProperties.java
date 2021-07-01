// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;

/**
 * The set of properties that comprise a connection string from the Azure portal.
 */
public class ConnectionStringProperties {
    private final ClientLogger logger = new ClientLogger(ConnectionStringProperties.class);

    private static final String TOKEN_VALUE_SEPARATOR = "=";
    private static final String ENDPOINT_SCHEME_SB_PREFIX = "sb://";
    private static final String ENDPOINT_SCHEME_HTTP_PREFIX = "http://";
    private static final String ENDPOINT_SCHEME_HTTPS_PREFIX = "https://";
    private static final String TOKEN_VALUE_PAIR_DELIMITER = ";";
    private static final String ENDPOINT = "Endpoint";
    private static final String SHARED_ACCESS_KEY_NAME = "SharedAccessKeyName";
    private static final String SHARED_ACCESS_KEY = "SharedAccessKey";
    private static final String SHARED_ACCESS_SIGNATURE = "SharedAccessSignature";
    private static final String SAS_VALUE_PREFIX = "sharedaccesssignature ";
    private static final String ENTITY_PATH = "EntityPath";
    private static final String CONNECTION_STRING_WITH_ACCESS_KEY = "Endpoint={endpoint};"
        + "SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={entityPath}";
    private static final String CONNECTION_STRING_WITH_SAS = "Endpoint={endpoint};SharedAccessSignature="
        + "SharedAccessSignature {sharedAccessSignature};EntityPath={entityPath}";
    private static final String ERROR_MESSAGE_FORMAT = String.format(Locale.US,
        "Could not parse 'connectionString'. Expected format: %s or %s.", CONNECTION_STRING_WITH_ACCESS_KEY,
        CONNECTION_STRING_WITH_SAS);

    private final URI endpoint;
    private final String entityPath;
    private final String sharedAccessKeyName;
    private final String sharedAccessKey;
    private final String sharedAccessSignature;

    /**
     * Creates a new instance by parsing the {@code connectionString} into its components.
     * @param connectionString The connection string to the Event Hub instance.
     *
     * @throws NullPointerException if {@code connectionString} is null.
     * @throws IllegalArgumentException if {@code connectionString} is an empty string or the connection string has
     * an invalid format.
     */
    public ConnectionStringProperties(String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        if (connectionString.isEmpty()) {
            throw new IllegalArgumentException("'connectionString' cannot be an empty string.");
        }

        final String[] tokenValuePairs = connectionString.split(TOKEN_VALUE_PAIR_DELIMITER);
        URI endpoint = null;
        String entityPath = null;
        String sharedAccessKeyName = null;
        String sharedAccessKeyValue = null;
        String sharedAccessSignature = null;

        for (String tokenValuePair : tokenValuePairs) {
            final String[] pair = tokenValuePair.split(TOKEN_VALUE_SEPARATOR, 2);
            if (pair.length != 2) {
                throw new IllegalArgumentException(String.format(
                    Locale.US,
                    "Connection string has invalid key value pair: %s",
                    tokenValuePair));
            }

            final String key = pair[0].trim();
            final String value = pair[1].trim();

            if (key.equalsIgnoreCase(ENDPOINT)) {
                final String endpointUri = validateAndUpdateDefaultScheme(value);
                try {
                    endpoint = new URI(endpointUri);
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(
                        String.format(Locale.US, "Invalid endpoint: %s", tokenValuePair), e);
                }
            } else if (key.equalsIgnoreCase(SHARED_ACCESS_KEY_NAME)) {
                sharedAccessKeyName = value;
            } else if (key.equalsIgnoreCase(SHARED_ACCESS_KEY)) {
                sharedAccessKeyValue = value;
            } else if (key.equalsIgnoreCase(ENTITY_PATH)) {
                entityPath = value;
            } else if (key.equalsIgnoreCase(SHARED_ACCESS_SIGNATURE)
                && value.toLowerCase(Locale.ROOT).startsWith(SAS_VALUE_PREFIX)) {
                sharedAccessSignature = value;
            } else {
                throw new IllegalArgumentException(
                    String.format(Locale.US, "Illegal connection string parameter name: %s", key));
            }
        }

        // connection string should have an endpoint and either shared access signature or shared access key and value
        boolean includesSharedKey = sharedAccessKeyName != null || sharedAccessKeyValue != null;
        boolean hasSharedKeyAndValue = sharedAccessKeyName != null && sharedAccessKeyValue != null;
        boolean includesSharedAccessSignature = sharedAccessSignature != null;
        if (endpoint == null
            || (includesSharedKey && includesSharedAccessSignature) // includes both SAS and key or value
            || (!hasSharedKeyAndValue && !includesSharedAccessSignature)) { // invalid key, value and SAS
            throw logger.logExceptionAsError(new IllegalArgumentException(ERROR_MESSAGE_FORMAT));
        }

        this.endpoint = endpoint;
        this.entityPath = entityPath;
        this.sharedAccessKeyName = sharedAccessKeyName;
        this.sharedAccessKey = sharedAccessKeyValue;
        this.sharedAccessSignature = sharedAccessSignature;
    }

    /**
     * Gets the endpoint to be used for connecting to the AMQP message broker.
     * @return The endpoint address, including protocol, from the connection string.
     */
    public URI getEndpoint() {
        return endpoint;
    }

    /**
     * Gets the entity path to connect to in the message broker.
     * @return The entity path to connect to in the message broker.
     */
    public String getEntityPath() {
        return entityPath;
    }

    /**
     * Gets the name of the shared access key, either for the Event Hubs namespace or the Event Hub instance.
     * @return The name of the shared access key.
     */
    public String getSharedAccessKeyName() {
        return sharedAccessKeyName;
    }

    /**
     * The value of the shared access key, either for the Event Hubs namespace or the Event Hub.
     * @return The value of the shared access key.
     */
    public String getSharedAccessKey() {
        return sharedAccessKey;
    }

    /**
     * The value of the shared access signature, if the connection string used to create this instance included the
     * shared access signature component.
     * @return The shared access signature value, if included in the connection string.
     */
    public String getSharedAccessSignature() {
        return sharedAccessSignature;
    }

    /*
     * The function checks for pre existing scheme of "sb://" , "http://" or "https://". If the scheme is not provided
     * in endpoint, it will set the default scheme to "sb://".
     */
    private String validateAndUpdateDefaultScheme(final String endpoint) {

        if (CoreUtils.isNullOrEmpty(endpoint)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'Endpoint' must be provided in 'connectionString'."));
        }

        final String endpointLowerCase = endpoint.trim().toLowerCase(Locale.ROOT);
        if (!endpointLowerCase.startsWith(ENDPOINT_SCHEME_SB_PREFIX)
            && !endpointLowerCase.startsWith(ENDPOINT_SCHEME_HTTP_PREFIX)
            && !endpointLowerCase.startsWith(ENDPOINT_SCHEME_HTTPS_PREFIX)) {
            return ENDPOINT_SCHEME_SB_PREFIX + endpoint;
        }
        return endpointLowerCase;
    }
}
