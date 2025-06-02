// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.core.credential.TokenCredential;

import java.util.Objects;

import static com.azure.communication.common.EntraCommunicationTokenUtils.*;
import static com.azure.core.util.CoreUtils.isNullOrEmpty;

/**
 * The Entra Communication Token Options.
 */
public class EntraCommunicationTokenCredentialOptions {

    private static final String[] DEFAULT_SCOPES = { DEFAULT_SCOPE };

    private final TokenCredential tokenCredential;
    private final String resourceEndpoint;
    private final String[] scopes;

    /**
     * Initializes a new instance of EntraCommunicationTokenCredentialOptions.
     *
     * @param tokenCredential  The credential capable of fetching an Entra user token.
     * @param resourceEndpoint The URI of the Azure Communication Services resource. For example, https://myResource.communication.azure.com.
     */
    public EntraCommunicationTokenCredentialOptions(TokenCredential tokenCredential, String resourceEndpoint) {
        this(tokenCredential, resourceEndpoint, DEFAULT_SCOPES);
    }

    /**
     * Initializes a new instance of EntraCommunicationTokenCredentialOptions with custom scopes.
     *
     * @param tokenCredential  The credential capable of fetching an Entra user token.
     * @param resourceEndpoint The URI of the Azure Communication Services resource. For example, https://myResource.communication.azure.com.
     * @param scopes           The scopes required for the Entra user token. These scopes determine the permissions granted to the token. For example, ["https://communication.azure.com/clients/VoIP"].
     */
    public EntraCommunicationTokenCredentialOptions(TokenCredential tokenCredential, String resourceEndpoint,
        String[] scopes) {
        Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        if (isNullOrEmpty(resourceEndpoint)) {
            throw new IllegalArgumentException("'resourceEndpoint' cannot be null or empty.");
        }
        this.resourceEndpoint = resourceEndpoint;
        this.tokenCredential = tokenCredential;
        this.scopes = validateScopes(scopes);
    }

    /**
     * Gets the credential capable of fetching an Entra user token.
     *
     * @return the token credential.
     */
    public TokenCredential getTokenCredential() {
        return tokenCredential;
    }

    /**
     * Gets the URI of the Azure Communication Services resource.
     *
     * @return the resource endpoint.
     */
    public String getResourceEndpoint() {
        return resourceEndpoint;
    }

    /**
     * Gets the scopes required for the Entra user token.
     *
     * @return the scopes.
     */
    public String[] getScopes() {
        return scopes.clone();
    }

    private static String[] validateScopes(String[] scopes) {
        if (scopes == null || scopes.length == 0) {
            throw new IllegalArgumentException("Scopes must not be null or empty. Ensure all scopes start with either "
                + TEAMS_EXTENSION_SCOPE_PREFIX + " or " + COMMUNICATION_CLIENTS_SCOPE_PREFIX + ".");
        }

        if (allScopesStartWith(scopes, TEAMS_EXTENSION_SCOPE_PREFIX)
            || allScopesStartWith(scopes, COMMUNICATION_CLIENTS_SCOPE_PREFIX)) {
            return scopes;
        }
        throw new IllegalArgumentException("Scopes validation failed. Ensure all scopes start with either "
            + TEAMS_EXTENSION_SCOPE_PREFIX + " or " + COMMUNICATION_CLIENTS_SCOPE_PREFIX + ".");
    }
}
