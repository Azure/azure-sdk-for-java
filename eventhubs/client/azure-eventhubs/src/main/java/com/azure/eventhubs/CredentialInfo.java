// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.implementation.util.ImplUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * A credential information object that contains all key-value pairs of ConnectionString
 */
public final class CredentialInfo {

    private static final String TOKEN_VALUE_SEPERATOR = "=";
    private static final String TOKEN_VALUE_PAIR_DELIMITER = ";";
    private static final String ENDPOINT = "Endpoint";
    private static final String SHARED_ACCESS_KEY_NAME = "SharedAccessKeyName";
    private static final String SHARED_ACCESS_KEY = "SharedAccessKey";
    private static final String ENTITY_PATH = "EntityPath";

    private URI endpoint;
    private String sharedAccessKeyName;
    private String sharedAccessKey;
    private String eventHubPath;

    protected CredentialInfo() { }

    /**
     * Create a {@link CredentialInfo} object that maps all key-value pairs of ConnectionString, include {@code EntityPath}.
     * Such as the connection string from 'SAS Policy: root' which contains {@code EntityPath}, well known as 'EventHub Path'.
     *
     * @param connectionString Connection String, which should at least include Endpoint, SharedAccessKeyName, SharedAccessKey and EntityPath.
     * @return a new created {@link CredentialInfo}.
     * @throws IllegalArgumentException when 'connectionString' is {@code null} or empty, or cannot be translated into an
     * {@link CredentialInfo}, or have invalid format.
     */
    public static CredentialInfo from(String connectionString) {
        return createCredentialInfo(connectionString, null);
    }

    /**
     * Create a {@link CredentialInfo} object that maps all key-value pairs of ConnectionString, exclude {@code EntityPath}.
     * Such as the connection string from 'SAS Policy: RootManageSharedAccessKey', which doesn't contain {@code EntityPath}.
     *
     * @param connectionString Connection String, which should at least include Endpoint, SharedAccessKeyName and SharedAccessKey.
     * @param eventHubPath EventHub Name that used in Azure Portal.
     * @return a new created {@link CredentialInfo}.
     * @throws IllegalArgumentException when 'connectionString' is {@code null} or empty, or cannot be translated into an
     * {@link CredentialInfo}, or have invalid format.
     */
    public static CredentialInfo from(String connectionString, String eventHubPath) {
        if (ImplUtils.isNullOrEmpty(eventHubPath)) {
            throw new IllegalArgumentException("EventHub path is null or empty");
        }
        return createCredentialInfo(connectionString, eventHubPath);
    }

    /**
     * Gets the Event Hubs namespace endpoint.
     *
     * @return The Event Hubs namespace endpoint.
     */
    public URI endpoint() {
        return endpoint;
    }

    /**
     * Gets the name of the shared access key.
     *
     * @return The name of the shared access key.
     */
    public String sharedAccessKeyName() {
        return sharedAccessKeyName;
    }

    /**
     * Gets the value of the shared access key.
     *
     * @return Value of the shared access key.
     */
    public String sharedAccessKey() {
        return sharedAccessKey;
    }

    /**
     * Gets the name of the Event Hub.
     *
     * @return Name of the Event Hub.
     */
    public String eventHubPath() {
        return eventHubPath;
    }

    private static CredentialInfo createCredentialInfo(String connectionString, String eventHubPath) {
        if (ImplUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalArgumentException("Connection string is null or empty");
        }

        final CredentialInfo credentialInfo = new CredentialInfo();
        final String[] tokenValuePairs = connectionString.split(TOKEN_VALUE_PAIR_DELIMITER);

        for (String tokenValuePair : tokenValuePairs) {
            final String[] pair = tokenValuePair.split(TOKEN_VALUE_SEPERATOR, 2);
            if (pair.length != 2) {
                throw new IllegalArgumentException(String.format(Locale.US, "Connection string has invalid key value pair: %s", tokenValuePair));
            }

            final String pairKey = pair[0].trim();
            final String pairValue = pair[1].trim();

            if (pairKey.equalsIgnoreCase(ENDPOINT)) {
                try {
                    credentialInfo.endpoint = new URI(pairValue);
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(String.format(Locale.US, "Invalid endpoint: %s", tokenValuePair), e);
                }
            } else if (pairKey.equalsIgnoreCase(SHARED_ACCESS_KEY_NAME)) {
                credentialInfo.sharedAccessKeyName = pairValue;
            } else if (pairKey.equalsIgnoreCase(SHARED_ACCESS_KEY)) {
                credentialInfo.sharedAccessKey = pairValue;
            } else if (pairKey.equalsIgnoreCase(ENTITY_PATH)) {
                credentialInfo.eventHubPath = pairValue;
            }
        }

        if (!ImplUtils.isNullOrEmpty(eventHubPath)) {
            credentialInfo.eventHubPath = eventHubPath;
        }

        if (credentialInfo.endpoint == null || credentialInfo.sharedAccessKeyName == null
            || credentialInfo.sharedAccessKey == null || credentialInfo.eventHubPath == null) {
            throw new IllegalArgumentException("Could not parse 'connectionString'."
                + "Expected format: 'Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
                + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}'. Actual:"
                + connectionString);
        }

        return credentialInfo;
    }

    public CredentialInfo clone() {
        CredentialInfo cloneObject = new CredentialInfo();
        cloneObject.sharedAccessKeyName = sharedAccessKeyName;
        cloneObject.sharedAccessKey = sharedAccessKey;
        cloneObject.endpoint = endpoint;
        cloneObject.eventHubPath = eventHubPath;
        return cloneObject;
    }
}
