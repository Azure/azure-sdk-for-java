// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link OnBehalfOfCredential}.
 *
 * @see OnBehalfOfCredential
 */
public class OnBehalfOfCredentialBuilder extends AadCredentialBuilderBase<OnBehalfOfCredentialBuilder> {
    private String clientSecret;
    private String clientCertificatePath;
    private String clientCertificatePassword;
    private final ClientLogger logger = new ClientLogger(OnBehalfOfCredentialBuilder.class);

    /**
     * Sets the client secret for the authentication.
     * @param clientSecret the secret value of the AAD application.
     * @return An updated instance of this builder.
     */
    public OnBehalfOfCredentialBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
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
    public OnBehalfOfCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions
                                                                          tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Sets the path and password of the PFX certificate for authenticating to AAD.
     *
     * @param certificatePath the password protected PFX file containing the certificate
     * @param clientCertificatePassword the password protecting the PFX file
     * @return An updated instance of this builder.
     */
    public OnBehalfOfCredentialBuilder pfxCertificate(String certificatePath,
                                                             String clientCertificatePassword) {
        this.clientCertificatePath = certificatePath;
        this.clientCertificatePassword = clientCertificatePassword;
        return this;
    }

    /**
     * Specifies if the x5c claim (public key of the certificate) should be sent as part of the authentication request
     * and enable subject name / issuer based authentication. The default value is false.
     *
     * @param sendCertificateChain the flag to indicate if certificate chain should be sent as part of authentication
     * request.
     * @return An updated instance of this builder.
     */
    public OnBehalfOfCredentialBuilder sendCertificateChain(boolean sendCertificateChain) {
        this.identityClientOptions.setIncludeX5c(sendCertificateChain);
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
    public OnBehalfOfCredentialBuilder regionalAuthority(RegionalAuthority regionalAuthority) {
        this.identityClientOptions.setRegionalAuthority(regionalAuthority);
        return this;
    }

    /**
     * Configure the User Assertion Scope to be used for OnBehalfOf Authentication request.
     *
     * @param userAssertion the user assertion access token to be used for On behalf Of authentication flow
     * @return An updated instance of this builder with the user assertion scope configured.
     */
    public OnBehalfOfCredentialBuilder userAssertion(String userAssertion) {
        this.identityClientOptions.userAssertion(userAssertion);
        return this;
    }

    /**
     * Creates a new {@link OnBehalfOfCredential} with the current configurations.
     *
     * @return a {@link OnBehalfOfCredential} with the current configurations.
     * @throws IllegalArgumentException if eiter both the client secret and certificate are configured or none of them
     * are configured.
     */
    public OnBehalfOfCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {
            {
                put("clientId", clientId);
                put("tenantId", tenantId);
            }
        });

        if (clientSecret == null && clientCertificatePath == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Atleast client secret or certificate "
                + "path should provided in OnBhealfOfCredentialBuilder. Only one of them should "
                + "be provided."));
        }

        if (clientCertificatePath != null && clientSecret != null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Both client secret and certificate "
                + "path are provided in OnBhealfCredentialBuilder. Only one of them should "
                + "be provided."));
        }

        return new OnBehalfOfCredential(clientId, tenantId, clientSecret, clientCertificatePath,
            clientCertificatePassword, identityClientOptions);
    }
}
