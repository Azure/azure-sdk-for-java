// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.Objects;

/**
 * Generates the correct resource scope to access Azure messaging resources given the authorization type.
 */
public class AzureTokenManagerProvider implements TokenManagerProvider {
    static final String TOKEN_AUDIENCE_FORMAT = "amqp://%s/%s";

    private final ClientLogger logger = new ClientLogger(AzureTokenManagerProvider.class);
    private final CbsAuthorizationType authorizationType;
    private final String fullyQualifiedNamespace;
    private final String activeDirectoryScope;

    /**
     * Creates an instance that provides {@link TokenManager} for the given {@code hostname} with the
     * {@code authorizationType}.
     *
     * @param authorizationType Method to authorize against Azure messaging service.
     * @param fullyQualifiedNamespace Fully-qualified namespace of the message broker.
     * @param activeDirectoryScope Scope used to access AD resources for the Azure service.
     */
    public AzureTokenManagerProvider(CbsAuthorizationType authorizationType, String fullyQualifiedNamespace,
                                     String activeDirectoryScope) {
        this.activeDirectoryScope = Objects.requireNonNull(activeDirectoryScope,
            "'activeDirectoryScope' cannot be null.");
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.authorizationType = Objects.requireNonNull(authorizationType,
            "'authorizationType' cannot be null.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TokenManager getTokenManager(Mono<ClaimsBasedSecurityNode> cbsNodeMono, String resource) {
        final String scopes = getScopesFromResource(resource);
        final String tokenAudience = String.format(Locale.US, TOKEN_AUDIENCE_FORMAT, fullyQualifiedNamespace, resource);

        logger.verbose("Creating new token manager for audience[{}], resource[{}]", tokenAudience, resource);

        return new ActiveClientTokenManager(cbsNodeMono, tokenAudience, scopes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScopesFromResource(String resource) {
        if (CbsAuthorizationType.JSON_WEB_TOKEN.equals(authorizationType)) {
            return activeDirectoryScope;
        } else if (CbsAuthorizationType.SHARED_ACCESS_SIGNATURE.equals(authorizationType)) {
            return String.format(Locale.US, TOKEN_AUDIENCE_FORMAT, fullyQualifiedNamespace, resource);
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
                "'%s' is not supported authorization type for token audience.", authorizationType)));
        }
    }
}
