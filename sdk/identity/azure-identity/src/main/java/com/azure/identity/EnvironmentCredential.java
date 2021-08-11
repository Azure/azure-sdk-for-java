// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.LoggingUtil;
import com.azure.identity.implementation.util.ValidationUtil;
import reactor.core.publisher.Mono;

/**
 * A credential provider that provides token credentials based on environment variables.  The environment variables
 * expected are:
 * <ul>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_ID AZURE_CLIENT_ID}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_SECRET AZURE_CLIENT_SECRET}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_TENANT_ID AZURE_TENANT_ID}</li>
 * </ul>
 * or:
 * <ul>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_ID AZURE_CLIENT_ID}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH AZURE_CLIENT_CERTIFICATE_PATH}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_TENANT_ID AZURE_TENANT_ID}</li>
 * </ul>
 * or:
 * <ul>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_ID AZURE_CLIENT_ID}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_USERNAME AZURE_USERNAME}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_PASSWORD AZURE_PASSWORD}</li>
 * </ul>
 */
@Immutable
public class EnvironmentCredential implements TokenCredential {
    private final Configuration configuration;
    private final ClientLogger logger = new ClientLogger(EnvironmentCredential.class);
    private final TokenCredential tokenCredential;

    /**
     * Creates an instance of the default environment credential provider.
     *
     * @param identityClientOptions the options for configuring the identity client
     */
    EnvironmentCredential(IdentityClientOptions identityClientOptions) {
        this.configuration = identityClientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration().clone() : identityClientOptions.getConfiguration();
        TokenCredential targetCredential = null;

        String clientId = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID);
        String tenantId = configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
        String clientSecret = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_SECRET);
        String certPath = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH);
        String username = configuration.get(Configuration.PROPERTY_AZURE_USERNAME);
        String password = configuration.get(Configuration.PROPERTY_AZURE_PASSWORD);
        ValidationUtil.validateTenantIdCharacterRange(getClass().getSimpleName(), tenantId);
        LoggingUtil.logAvailableEnvironmentVariables(logger, configuration);
        if (verifyNotNull(clientId)) {
            // 1 - Attempt ClientSecretCredential or ClientCertificateCredential
            if (verifyNotNull(tenantId)) {
                if (verifyNotNull(clientSecret)) {
                    // 1.1 Attempt ClientSecretCredential
                    logger.info("Azure Identity => EnvironmentCredential invoking ClientSecretCredential");
                    targetCredential = new ClientSecretCredential(tenantId, clientId, clientSecret,
                        identityClientOptions);
                } else if (verifyNotNull(certPath)) {
                    // 1.2 Attempt ClientCertificateCredential
                    logger.info("Azure Identity => EnvironmentCredential invoking ClientCertificateCredential");
                    targetCredential = new ClientCertificateCredential(tenantId, clientId, certPath, null, null,
                            identityClientOptions);
                } else {
                    // 1.3 Log error if neither is found
                    logger.error("Azure Identity => ERROR in EnvironmentCredential: Failed to create a "
                        + "ClientSecretCredential or ClientCertificateCredential. Missing required environment "
                        + "variable either {} or {}", Configuration.PROPERTY_AZURE_CLIENT_SECRET,
                        Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH);
                }
            } else if (verifyNotNull(clientSecret) || verifyNotNull(certPath)) {
                // 1.4 Log error if secret / cert is found but tenant is missing
                logger.error("Azure Identity => ERROR in EnvironmentCredential: Failed to create a "
                        + "ClientSecretCredential or ClientCertificateCredential. Missing required environment "
                        + "variable {}", Configuration.PROPERTY_AZURE_TENANT_ID);
            }

            // 2 - Attempt UsernamePasswordCredential (tenant not required)
            if (targetCredential == null && verifyNotNull(username, password)) {
                // 2.1 - both username and password found
                logger.info("Azure Identity => EnvironmentCredential invoking UsernamePasswordCredential");
                targetCredential = new UsernamePasswordCredential(clientId, tenantId, username, password,
                    identityClientOptions);
            } else if (verifyNotNull(username) ^ verifyNotNull(password)) {
                // 2.2 - only one is found, likely missing the other
                logger.error("Azure Identity => ERROR in EnvironmentCredential: Failed to create a "
                    + "UsernamePasswordCredential. Missing required environment variable {}",
                    username == null ? Configuration.PROPERTY_AZURE_USERNAME : Configuration.PROPERTY_AZURE_PASSWORD);
            }

            // 3 - cannot determine scenario based on clientId alone
            if (targetCredential == null) {
                String msg = String.format("Azure Identity => ERROR in EnvironmentCredential: Failed to determine an "
                    + "authentication scheme based on the available environment variables. Please specify %1$s and "
                    + "%2$s to authenticate through a ClientSecretCredential; %1$s and %3$s to authenticate through a "
                    + "ClientCertificateCredential; or %4$s and %5$s to authenticate through a "
                    + "UserPasswordCredential.", Configuration.PROPERTY_AZURE_TENANT_ID,
                    Configuration.PROPERTY_AZURE_CLIENT_SECRET, Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH,
                    Configuration.PROPERTY_AZURE_USERNAME, Configuration.PROPERTY_AZURE_PASSWORD);
                logger.error(msg);
            }
        } else {
            // 4 - not even clientId is available
            logger.error("Azure Identity => ERROR in EnvironmentCredential: Missing required environment variable {}",
                Configuration.PROPERTY_AZURE_CLIENT_ID);
        }
        tokenCredential = targetCredential;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        if (tokenCredential == null) {
            return Mono.error(logger.logExceptionAsError(new CredentialUnavailableException(
                    "EnvironmentCredential authentication unavailable."
                            + " Environment variables are not fully configured.")));
        } else {
            return tokenCredential.getToken(request);
        }
    }

    private boolean verifyNotNull(String... configs) {
        for (String config: configs) {
            if (config == null) {
                return false;
            }
        }
        return true;
    }
}
