// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.LoggingUtil;
import com.azure.identity.implementation.util.ValidationUtil;
import reactor.core.publisher.Mono;

/**
 * <p>The EnvironmentCredential is appropriate for scenarios where the application is looking to read credential
 * information from environment variables. The credential supports service principal and user credential based
 * authentication and requires a set of environment variables to be configured for each scenario.</p>
 *
 * <p>The required environment variables for service principal authentication using client secret are as follows:</p>
 * <ul>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_ID AZURE_CLIENT_ID}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_SECRET AZURE_CLIENT_SECRET}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_TENANT_ID AZURE_TENANT_ID}</li>
 * </ul>
 *
 * <p>The required environment variables for service principal authentication using client certificate are as follows:
 * </p>
 * <ul>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_ID AZURE_CLIENT_ID}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH AZURE_CLIENT_CERTIFICATE_PATH}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_CERTIFICATE_PASSWORD AZURE_CLIENT_CERTIFICATE_PASSWORD}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_TENANT_ID AZURE_TENANT_ID}</li>
 * </ul>
 *
 * <p>The required environment variables for username password authentication are as follows:</p>
 * <ul>
 *     <li>{@link Configuration#PROPERTY_AZURE_CLIENT_ID AZURE_CLIENT_ID}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_USERNAME AZURE_USERNAME}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_PASSWORD AZURE_PASSWORD}</li>
 *     <li>{@link Configuration#PROPERTY_AZURE_TENANT_ID AZURE_TENANT_ID}</li>
 * </ul>
 *
 * <p>The credential looks for authentication scenarios in the order above, so ensure that only targeted authentication
 * scenario's environment variables are configured.</p>
 *
 * <p><strong>Sample: Construct EnvironmentCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.EnvironmentCredential},
 * using the {@link com.azure.identity.EnvironmentCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.environmentcredential.construct -->
 * <pre>
 * TokenCredential environmentCredential = new EnvironmentCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.environmentcredential.construct -->
 *
 * @see com.azure.identity
 * @see EnvironmentCredentialBuilder
 */
@Immutable
public class EnvironmentCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(EnvironmentCredential.class);
    private final TokenCredential tokenCredential;
    private final IdentityClientOptions identityClientOptions;

    /**
     * Creates an instance of the default environment credential provider.
     *
     * @param identityClientOptions the options for configuring the identity client
     */
    EnvironmentCredential(IdentityClientOptions identityClientOptions) {
        Configuration configuration = identityClientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration().clone() : identityClientOptions.getConfiguration();
        TokenCredential targetCredential = null;
        this.identityClientOptions = identityClientOptions;

        String clientId = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID);
        String tenantId = configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
        String clientSecret = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_SECRET);
        String certPath = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH);
        String certPassword = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PASSWORD);
        String username = configuration.get(Configuration.PROPERTY_AZURE_USERNAME);
        String password = configuration.get(Configuration.PROPERTY_AZURE_PASSWORD);
        if (CoreUtils.isNullOrEmpty(identityClientOptions.getAdditionallyAllowedTenants())) {
            identityClientOptions
                .setAdditionallyAllowedTenants(IdentityUtil.getAdditionalTenantsFromEnvironment(configuration));
        }
        ValidationUtil.validateTenantIdCharacterRange(tenantId, LOGGER);
        LoggingUtil.logAvailableEnvironmentVariables(LOGGER, configuration);
        if (verifyNotNull(clientId)) {
            // 1 - Attempt ClientSecretCredential or ClientCertificateCredential
            if (verifyNotNull(tenantId)) {
                if (verifyNotNull(clientSecret)) {
                    // 1.1 Attempt ClientSecretCredential
                    LOGGER.info("Azure Identity => EnvironmentCredential invoking ClientSecretCredential");
                    targetCredential = new ClientSecretCredential(tenantId, clientId, clientSecret,
                        identityClientOptions);
                } else if (verifyNotNull(certPath)) {
                    // 1.2 Attempt ClientCertificateCredential
                    LOGGER.info("Azure Identity => EnvironmentCredential invoking ClientCertificateCredential");
                    targetCredential = new ClientCertificateCredential(tenantId, clientId, certPath, null, certPassword,
                            identityClientOptions);
                } else {
                    // 1.3 Log error if neither is found
                    LoggingUtil.logError(LOGGER, identityClientOptions,
                        () -> String.format("Azure Identity => ERROR in EnvironmentCredential: Failed to create a "
                        + "ClientSecretCredential or ClientCertificateCredential. Missing required environment "
                        + "variable either %s or %s", Configuration.PROPERTY_AZURE_CLIENT_SECRET,
                        Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH));
                }
            } else if (verifyNotNull(clientSecret) || verifyNotNull(certPath)) {
                // 1.4 Log error if secret / cert is found but tenant is missing
                LoggingUtil.logError(LOGGER, identityClientOptions,
                    () -> String.format("Azure Identity => ERROR in EnvironmentCredential: Failed to create a "
                        + "ClientSecretCredential or ClientCertificateCredential. Missing required environment "
                        + "variable %s", Configuration.PROPERTY_AZURE_TENANT_ID));
            }

            // 2 - Attempt UsernamePasswordCredential (tenant not required)
            if (targetCredential == null && verifyNotNull(username, password)) {
                // 2.1 - both username and password found
                LOGGER.info("Azure Identity => EnvironmentCredential invoking UsernamePasswordCredential");
                targetCredential = new UsernamePasswordCredential(clientId, tenantId, username, password,
                    identityClientOptions);
            } else if (verifyNotNull(username) ^ verifyNotNull(password)) {
                // 2.2 - only one is found, likely missing the other
                LoggingUtil.logError(LOGGER, identityClientOptions,
                    () -> String.format("Azure Identity => ERROR in EnvironmentCredential: Failed to create a "
                    + "UsernamePasswordCredential. Missing required environment variable %s",
                    username == null ? Configuration.PROPERTY_AZURE_USERNAME : Configuration.PROPERTY_AZURE_PASSWORD));
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
                LoggingUtil.logError(LOGGER, identityClientOptions, msg);
            }
        } else {
            // 4 - not even clientId is available
            LoggingUtil.logError(LOGGER, identityClientOptions,
                () -> String.format("Azure Identity => ERROR in EnvironmentCredential:"
                        + " Missing required environment variable %s", Configuration.PROPERTY_AZURE_CLIENT_ID));
        }
        tokenCredential = targetCredential;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        if (tokenCredential == null) {
            return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, identityClientOptions,
                new CredentialUnavailableException(
                    "EnvironmentCredential authentication unavailable."
                        + " Environment variables are not fully configured."
                        + "To mitigate this issue, please refer to the troubleshooting guidelines here at"
                        + " https://aka.ms/azsdk/java/identity/environmentcredential/troubleshoot")));
        } else {
            return tokenCredential.getToken(request);
        }
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        if (tokenCredential == null) {
            throw LoggingUtil.logCredentialUnavailableException(LOGGER, identityClientOptions,
                new CredentialUnavailableException(
                    "EnvironmentCredential authentication unavailable."
                        + " Environment variables are not fully configured."
                        + "To mitigate this issue, please refer to the troubleshooting guidelines here at"
                        + " https://aka.ms/azsdk/java/identity/environmentcredential/troubleshoot"));
        } else {
            return tokenCredential.getTokenSync(request);
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
