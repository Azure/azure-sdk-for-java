// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.implementation.util.ImplUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public final class CredentialInfo {

    private static final String ENDPOINT = "Endpoint=";
    private static final String SHARED_ACCESS_KEY_NAME = "SharedAccessKeyName=";
    private static final String SHARED_ACCESS_KEY = "SharedAccessKey=";
    private static final String ENTITY_PATH = "EntityPath=";

    private URI endpoint;
    private String sharedAccessKeyName;
    private String sharedAccessKey;
    private String eventHubName;

    private CredentialInfo() { }

    /**
     * Create a CredentialInfo object from connection string.
     *
     * @param connectionString Connection String, which should at least include Endpoint, SharedAccessKeyName and SharedAccessKey
     * @return CredentialInfo
     * @throws IllegalArgumentException when 'connectionString' is {@code null} or empty, or cannot be translated into an
     * {@link CredentialInfo}, or have invalid format.
     */
    public static CredentialInfo from(String connectionString) {

        CredentialInfo credentialInfo = new CredentialInfo();
        if (ImplUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalArgumentException("connection string is null or empty.");
        }


        String[] args = connectionString.toLowerCase(Locale.ENGLISH).split(";");

        for (String arg : args) {
            String segment = arg.trim();

            if (segment.startsWith(ENDPOINT.toLowerCase(Locale.ENGLISH))) {
                try {
                    credentialInfo.endpoint = new URI(segment.substring(ENDPOINT.length()));
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(String.format(Locale.US, "Invalid endpoint: %s", segment), e);
                }
            } else if (segment.startsWith(SHARED_ACCESS_KEY_NAME.toLowerCase(Locale.ENGLISH))) {
                credentialInfo.sharedAccessKeyName = segment.substring(SHARED_ACCESS_KEY_NAME.length());
            } else if (segment.startsWith(SHARED_ACCESS_KEY.toLowerCase(Locale.ENGLISH))) {
                credentialInfo.sharedAccessKey = segment.substring(SHARED_ACCESS_KEY.length());
            } else if (segment.startsWith(ENTITY_PATH.toLowerCase(Locale.ENGLISH))) {
                credentialInfo.eventHubName = segment.substring(ENTITY_PATH.length());
            }
        }

        if (credentialInfo.endpoint == null || credentialInfo.sharedAccessKeyName == null
            || credentialInfo.sharedAccessKey == null || credentialInfo.eventHubName == null) {
            throw new IllegalArgumentException("Could not parse 'connectionString'."
                + "Expected format: 'Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={entityPath}'. Actual:"
                + connectionString);
        }
        return credentialInfo;
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

    String eventHubName() {
        return eventHubName;
    }
}
