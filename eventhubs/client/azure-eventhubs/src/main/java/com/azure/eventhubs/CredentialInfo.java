// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.implementation.util.ImplUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

/**
 * Builder to create an EventHub Credentials Information from given ConnectionString and/or EventHub Path
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

    private CredentialInfo() { }

    /**
     * Create a CredentialInfo object from connection string that contains 'EntityPath' key value pair, also well known as 'EventHub Name'.
     *
     * @param connectionString Connection String, which should at least include Endpoint, SharedAccessKeyName and SharedAccessKey
     * @return CredentialInfo
     * @throws IllegalArgumentException when 'connectionString' is {@code null} or empty, or cannot be translated into an
     * {@link CredentialInfo}, or have invalid format.
     */
    public static CredentialInfo from(String connectionString) {
        return createCredentialInfo(connectionString, null);
    }

    /**
     * Create a CredentialInfo object from connection string which doesn't contain 'EntityPath' key but provided it in this method.
     *
     * @param connectionString Connection String, which should at least include Endpoint, SharedAccessKeyName and SharedAccessKey
     * @param eventHubPath EventHub Name that used in Azure Portal
     * @return CredentialInfo
     * @throws IllegalArgumentException when 'connectionString' is {@code null} or empty, or cannot be translated into an
     * {@link CredentialInfo}, or have invalid format.
     */
    public static CredentialInfo from(String connectionString, String eventHubPath) {
        if (ImplUtils.isNullOrEmpty(eventHubPath)) {
            throw new IllegalArgumentException("EventHub path is null or empty.");
        }
        return createCredentialInfo(connectionString, eventHubPath);
    }

    URI endpoint() {
        return this.endpoint;
    }

    String sharedAccessKeyName() {
        return this.sharedAccessKeyName;
    }

    String sharedAccessKey() {
        return sharedAccessKey;
    }

    String eventHubPath() {
        return eventHubPath;
    }

    private static CredentialInfo createCredentialInfo(String connectionString, String eventHubPath) {
        if (ImplUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalArgumentException("Connection string is null or empty.");
        }
        CredentialInfo credentialInfo = new CredentialInfo();
        String[] tokenValuePairs = connectionString.split(TOKEN_VALUE_PAIR_DELIMITER);
        for (String tokenValuePair : tokenValuePairs) {
            String[] pair = tokenValuePair.split(TOKEN_VALUE_SEPERATOR);
            if (pair.length != 2) {
                throw new IllegalArgumentException(String.format(Locale.US,"Connection string has invalid key value pair: %s", tokenValuePair));
            }

            String pairKey = pair[0].trim();
            String pairValue = pair[1].trim();

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
}
