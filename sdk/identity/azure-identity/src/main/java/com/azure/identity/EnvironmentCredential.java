// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientOptions;
import reactor.core.publisher.Mono;

/**
 * A credential provider that provides token credentials based on environment variables.  The sets of environment variables
 * expected are:
 * <p>
 * <ul>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_ID AZURE_CLIENT_ID}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_SECRET AZURE_CLIENT_SECRET}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_TENANT_ID AZURE_TENANT_ID}</li>
 * </ul>
 * or:
 * <p>
 * <ul>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_ID AZURE_CLIENT_ID}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH AZURE_CLIENT_CERTIFICATE_PATH}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_TENANT_ID AZURE_TENANT_ID}</li>
 * </ul>
 * or:
 * <p>
 * <ul>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_ID AZURE_CLIENT_ID}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_USERNAME AZURE_USERNAME}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_PASSWORD AZURE_PASSWORD}</li>
 * </ul>
 */
@Immutable
public class EnvironmentCredential implements TokenCredential {
    private final Configuration configuration;
    private final IdentityClientOptions identityClientOptions;
    private final ClientLogger logger = new ClientLogger(EnvironmentCredential.class);

    /**
     * Creates an instance of the default environment credential provider.
     *
     * @param identityClientOptions the options for configuring the identity client
     */
    EnvironmentCredential(IdentityClientOptions identityClientOptions) {
        this.configuration = Configuration.getGlobalConfiguration().clone();
        this.identityClientOptions = identityClientOptions;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        String clientId = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID);
        String tenantId = configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
        String clientSecret = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_SECRET);
        String certPath = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH);
        String username = configuration.get(Configuration.PROPERTY_AZURE_USERNAME);
        String password = configuration.get(Configuration.PROPERTY_AZURE_PASSWORD);
        return Mono.fromSupplier(() -> {
            if (verifyNotNull(clientId)) {
                if (verifyNotNull(tenantId, clientSecret)) {
                    // TODO: support other clouds
                    return new ClientSecretCredential(tenantId, clientId, clientSecret, identityClientOptions);
                } else if (verifyNotNull(tenantId, certPath)) {
                    return new ClientCertificateCredential(tenantId, clientId, certPath, null, identityClientOptions);
                } else if (verifyNotNull(username, password)) {
                    return new UsernamePasswordCredential(clientId, tenantId, username, password, identityClientOptions);
                }
            }

            // Other environment variables
            throw logger.logExceptionAsError(new ClientAuthenticationException(
                "Cannot create any credentials with the current environment variables",
                null));
        }).flatMap(cred -> cred.getToken(request));
    }

    private boolean verifyNotNull(String... configs){
        for(String config: configs){
            if(config == null){
                return false;
            }
        }
        return true;
    }
}
