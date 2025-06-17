// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.core.credential.TokenCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.azure.communication.common.EntraCommunicationTokenUtils.allScopesStartWith;
import static com.azure.communication.common.EntraCommunicationTokenUtils.TEAMS_EXTENSION_SCOPE_PREFIX;
import static com.azure.communication.common.EntraCommunicationTokenUtils.COMMUNICATION_CLIENTS_SCOPE_PREFIX;
import static com.azure.communication.common.EntraCommunicationTokenUtils.COMMUNICATION_CLIENTS_DEFAULT_SCOPE;
import static com.azure.core.util.CoreUtils.isNullOrEmpty;

/**
 * The Entra Communication Token Options.
 */
public final class EntraCommunicationTokenCredentialOptions {

    private final TokenCredential tokenCredential;
    private final String resourceEndpoint;
    private List<String> scopes;

    /**
     * Initializes a new instance of EntraCommunicationTokenCredentialOptions with default scopes.
     *
     * @param tokenCredential The credential capable of fetching an Entra user token.
     * @param resourceEndpoint The URI of the Azure Communication Services resource. For example, https://myResource.communication.azure.com.
     *
     * @throws NullPointerException if tokenCredential is null.
     * @throws IllegalArgumentException if resourceEndpoint is null or empty.
     */
    public EntraCommunicationTokenCredentialOptions(TokenCredential tokenCredential, String resourceEndpoint) {
        Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        if (isNullOrEmpty(resourceEndpoint)) {
            throw new IllegalArgumentException("'resourceEndpoint' cannot be null or empty.");
        }
        this.resourceEndpoint = resourceEndpoint;
        this.tokenCredential = tokenCredential;
        this.scopes = new ArrayList<String>() {
            {
                add(COMMUNICATION_CLIENTS_DEFAULT_SCOPE);
            }
        };
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
    public List<String> getScopes() {
        return scopes;
    }

    /**
     * Sets the scopes required for the Entra user token. These scopes determine the permissions granted to the token.
     * For example, ["https://communication.azure.com/clients/VoIP"].
     *
     * @param scopes The scopes to set.
     * @throws IllegalArgumentException if scopes are null, empty, or invalid.
     * @return this object
     */
    public EntraCommunicationTokenCredentialOptions setScopes(List<String> scopes) {
        this.scopes = validateScopes(scopes);
        return this;
    }

    private static List<String> validateScopes(List<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
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
