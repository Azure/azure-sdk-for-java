// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialUnavailableException;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.util.IdentityUtil;
import com.azure.v2.identity.implementation.util.LoggingUtil;
import com.azure.v2.identity.implementation.util.ValidationUtil;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;

/**
 * <p>The EnvironmentCredential is appropriate for scenarios where the application is looking to read credential
 * information from environment variables. The credential supports service principal and user credential based
 * authentication and requires a set of environment variables to be configured for each scenario.</p>
 *
 * <p>The required environment variables for service principal authentication using client secret are as follows:</p>
 * <ul>
 *     <li>{AZURE_CLIENT_ID}</li>
 *     <li>{AZURE_CLIENT_SECRET}</li>
 *     <li>{AZURE_TENANT_ID}</li>
 * </ul>
 *
 * <p>The required environment variables for service principal authentication using client certificate are as follows:
 * </p>
 * <ul>
 *     <li>{AZURE_CLIENT_ID}</li>
 *     <li>{AZURE_CLIENT_CERTIFICATE_PATH}</li>
 *     <li>{AZURE_CLIENT_CERTIFICATE_PASSWORD}</li>
 *     <li>{AZURE_TENANT_ID}</li>
 * </ul>
 *
 * <p>The credential looks for authentication scenarios in the order above, so ensure that only targeted authentication
 * scenario's environment variables are configured.</p>
 *
 * <p><strong>Sample: Construct EnvironmentCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link EnvironmentCredential},
 * using the {@link EnvironmentCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <pre>
 * TokenCredential environmentCredential = new EnvironmentCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 *
 * @see com.azure.v2.identity
 * @see EnvironmentCredentialBuilder
 */
public class EnvironmentCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(EnvironmentCredential.class);
    private final TokenCredential tokenCredential;

    /**
     * Creates an instance of the default environment credential provider.
     *
     * @param confidentialClientOptions the options for configuring the confidential client
     */
    EnvironmentCredential(ConfidentialClientOptions confidentialClientOptions) {
        Configuration configuration = confidentialClientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration()
            : confidentialClientOptions.getConfiguration();
        TokenCredential targetCredential = null;

        String clientId = configuration.get(IdentityUtil.PROPERTY_AZURE_CLIENT_ID);
        String tenantId = configuration.get(IdentityUtil.PROPERTY_AZURE_TENANT_ID);
        String clientSecret = configuration.get(IdentityUtil.PROPERTY_AZURE_CLIENT_SECRET);
        String certPath = configuration.get(IdentityUtil.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH);
        String sendCertificateChain
            = configuration.get(IdentityUtil.PROPERTY_AZURE_CLIENT_SEND_CERTIFICATE_CHAIN, "false");

        if (CoreUtils.isNullOrEmpty(confidentialClientOptions.getAdditionallyAllowedTenants())) {
            confidentialClientOptions
                .setAdditionallyAllowedTenants(IdentityUtil.getAdditionalTenantsFromEnvironment(configuration));
        }
        ValidationUtil.validateTenantIdCharacterRange(tenantId, LOGGER);
        LoggingUtil.logAvailableEnvironmentVariables(LOGGER, configuration);
        if (verifyNotNull(clientId)) {
            // 1 - Attempt ClientSecretCredential or ClientCertificateCredential
            if (verifyNotNull(tenantId)) {
                if (verifyNotNull(clientSecret)) {
                    // 1.1 Attempt ClientSecretCredential
                    LOGGER.atInfo().log("Azure Identity => EnvironmentCredential invoking ClientSecretCredential");
                    confidentialClientOptions.setClientSecret(clientSecret).setClientId(clientId).setTenantId(tenantId);
                    targetCredential = new ClientSecretCredential(confidentialClientOptions);
                } else if (verifyNotNull(certPath)) {
                    // 1.2 Attempt ClientCertificateCredential
                    LOGGER.atInfo().log("Azure Identity => EnvironmentCredential invoking ClientCertificateCredential");

                    if ("true".equalsIgnoreCase(sendCertificateChain) || "1".equals(sendCertificateChain)) {
                        confidentialClientOptions.setIncludeX5c(true);
                    }

                    confidentialClientOptions.setCertificatePath(certPath).setClientId(clientId).setTenantId(tenantId);

                    targetCredential = new ClientCertificateCredential(confidentialClientOptions);
                } else {
                    // 1.3 Log error if neither is found
                    LOGGER.atError()
                        .log(() -> String.format(
                            "Azure Identity => ERROR in EnvironmentCredential: Failed to create a "
                                + "ClientSecretCredential or ClientCertificateCredential. Missing required environment "
                                + "variable either %s or %s",
                            IdentityUtil.PROPERTY_AZURE_CLIENT_SECRET,
                            IdentityUtil.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH));
                }
            } else if (verifyNotNull(clientSecret) || verifyNotNull(certPath)) {
                // 1.4 Log error if secret / cert is found but tenant is missing
                LOGGER.atError()
                    .log(() -> String.format("Azure Identity => ERROR in EnvironmentCredential: Failed to create a "
                        + "ClientSecretCredential or ClientCertificateCredential. Missing required environment "
                        + "variable %s", IdentityUtil.PROPERTY_AZURE_TENANT_ID));
            }

            // 2 - cannot determine scenario based on clientId alone
            if (targetCredential == null) {
                LOGGER.atError()
                    .log(() -> String.format("Azure Identity => ERROR in EnvironmentCredential: Failed to determine an "
                        + "authentication scheme based on the available environment variables. Please specify %1$s and "
                        + "%2$s to authenticate through a ClientSecretCredential or %1$s and %3$s to authenticate through a "
                        + "ClientCertificateCredential.", IdentityUtil.PROPERTY_AZURE_TENANT_ID,
                        IdentityUtil.PROPERTY_AZURE_CLIENT_SECRET,
                        IdentityUtil.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH));
            }
        } else {
            // 3 - not even clientId is available
            LOGGER.atError()
                .log(() -> String.format(
                    "Azure Identity => ERROR in EnvironmentCredential:" + " Missing required environment variable %s",
                    IdentityUtil.PROPERTY_AZURE_CLIENT_ID));
        }
        tokenCredential = targetCredential;
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        if (tokenCredential == null) {
            throw LOGGER.throwableAtError()
                .log(
                    "EnvironmentCredential authentication unavailable."
                        + " Environment variables are not fully configured."
                        + "To mitigate this issue, please refer to the troubleshooting guidelines here at"
                        + " https://aka.ms/azsdk/java/identity/environmentcredential/troubleshoot",
                    CredentialUnavailableException::new);
        } else {
            return tokenCredential.getToken(request);
        }
    }

    private boolean verifyNotNull(String... configs) {
        for (String config : configs) {
            if (config == null) {
                return false;
            }
        }
        return true;
    }
}
