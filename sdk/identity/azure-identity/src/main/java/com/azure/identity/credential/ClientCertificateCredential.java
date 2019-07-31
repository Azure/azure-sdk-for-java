// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.implementation.annotation.Immutable;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientOptions;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * An AAD credential that acquires a token with a client certificate for an AAD application.
 */
@Immutable
public class ClientCertificateCredential implements TokenCredential {
    private String clientCertificate;
    private String clientCertificatePassword;
    private final IdentityClient identityClient;

    /**
     * Creates a ClientSecretCredential with default identity client options.
     * @param tenantId     the tenant ID of the application
     * @param clientId     the client ID of the application
     * @param certificatePath the PEM file / PFX file containing the certificate
     * @param certificatePassword the password protecting the PFX file
     * @param identityClientOptions the options to configure the identity client
     */
    ClientCertificateCredential(String tenantId, String clientId, String certificatePath, String certificatePassword, IdentityClientOptions identityClientOptions) {
        Objects.requireNonNull(certificatePath);
        this.clientCertificate = certificatePath;
        this.clientCertificatePassword = certificatePassword;
        this.identityClient = new IdentityClient(tenantId, clientId, identityClientOptions);
    }

    @Override
    public Mono<AccessToken> getToken(String... scopes) {
        if (clientCertificate == null) {
            return Mono.error(new IllegalArgumentException("Non-null value must be provided for clientCertificate property in ClientCertificateCredential"));
        }
        if (clientCertificatePassword != null) {
            return identityClient.authenticateWithPfxCertificate(clientCertificate, clientCertificatePassword, scopes);
        } else {
            return identityClient.authenticateWithPemCertificate(clientCertificate, scopes);
        }
    }
}
