// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.CBSNode;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.Objects;

/**
 * Generates the correct resource scope to access Azure messaging resources given the authorization type.
 */
public class AzureTokenManagerProvider implements TokenManagerProvider {
    private static final String TOKEN_AUDIENCE_FORMAT = "amqp://%s/%s";

    private final ClientLogger logger = new ClientLogger(AzureTokenManagerProvider.class);
    private final CBSAuthorizationType authorizationType;
    private final String host;
    private final String activeDirectoryScope;

    /**
     * Creates an instance that provides {@link TokenManager} for the given {@code host} with the {@code
     * authorizationType}.
     *
     * @param authorizationType Method to authorize against Azure messaging service.
     * @param host Fully-qualified domain name (FQDN) of the message broker.
     * @param activeDirectoryScope Scope used to access AD resources for the Azure service.
     */
    public AzureTokenManagerProvider(CBSAuthorizationType authorizationType, String host, String activeDirectoryScope) {
        this.activeDirectoryScope = Objects.requireNonNull(activeDirectoryScope,
            "'activeDirectoryScope' cannot be null.");
        this.host = Objects.requireNonNull(host, "'host' cannot be null.");
        this.authorizationType = Objects.requireNonNull(authorizationType,
            "'authorizationType' cannot be null.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TokenManager getTokenManager(Mono<CBSNode> cbsNodeMono, String resource) {
        final String audience = getResourceString(resource);
        return new ActiveClientTokenManager(cbsNodeMono, audience);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResourceString(String resource) {
        switch (authorizationType) {
            case JSON_WEB_TOKEN:
                return activeDirectoryScope;
            case SHARED_ACCESS_SIGNATURE:
                return String.format(Locale.US, TOKEN_AUDIENCE_FORMAT, host, resource);
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
                    "'%s' is not supported authorization type for token audience.", authorizationType)));
        }
    }
}
