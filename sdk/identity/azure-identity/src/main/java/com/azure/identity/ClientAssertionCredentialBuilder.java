// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.RegionalAuthority;
import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Fluent credential builder for instantiating a {@link ClientAssertionCredential}.
 *
 * @see ClientAssertionCredential
 */
public class ClientAssertionCredentialBuilder extends AadCredentialBuilderBase<ClientAssertionCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(ClientAssertionCredentialBuilder.class);

    private Supplier<String> clientAssertionSupplier;

    /**
     * Sets the supplier containing the logic to supply the client assertion when invoked.
     *
     * @param clientAssertionSupplier the supplier supplying client assertion.
     * @return An updated instance of this builder.
     */
    public ClientAssertionCredentialBuilder clientAssertion(Supplier<String> clientAssertionSupplier) {
        this.clientAssertionSupplier = clientAssertionSupplier;
        return this;
    }

    /**
     * Configures the persistent shared token cache options and enables the persistent token cache which is disabled
     * by default. If configured, the credential will store tokens in a cache persisted to the machine, protected to
     * the current user, which can be shared by other credentials and processes.
     *
     * @param tokenCachePersistenceOptions the token cache configuration options
     * @return An updated instance of this builder with the token cache options configured.
     */
    public ClientAssertionCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions
                                                                          tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Specifies either the specific regional authority, or use {@link RegionalAuthority#AUTO_DISCOVER_REGION} to
     * attempt to auto-detect the region. If unset, a non-regional authority will be used. This argument should be used
     * only by applications deployed to Azure VMs.
     *
     * @param regionalAuthority the regional authority
     * @return An updated instance of this builder with the regional authority configured.
     */
    ClientAssertionCredentialBuilder regionalAuthority(RegionalAuthority regionalAuthority) {
        this.identityClientOptions.setRegionalAuthority(regionalAuthority);
        return this;
    }

    /**
     * Creates a new {@link ClientAssertionCredential} with the current configurations.
     *
     * @return a {@link ClientAssertionCredential} with the current configurations.
     * @throws IllegalArgumentException if either of clientId, tenantId or clientAssertion is not present.
     */
    public ClientAssertionCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("clientId", clientId);
                put("tenantId", tenantId);
                put("clientAssertion", clientAssertionSupplier);
            }}, LOGGER);

        return new ClientAssertionCredential(clientId, tenantId, clientAssertionSupplier, identityClientOptions);
    }
}
