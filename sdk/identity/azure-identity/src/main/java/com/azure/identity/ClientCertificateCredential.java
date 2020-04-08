// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * An AAD credential that acquires a token with a client certificate for an AAD application.
 *
 * <p><strong>Sample: Construct a simple ClientCertificateCredential</strong></p>
 * <pre>
 * ClientCertificateCredential credential1 = new ClientCertificateCredentialBuilder&#40;&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .pemCertificate&#40;&quot;C:&#92;&#92;fakepath&#92;&#92;cert.pem&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 *
 * <p><strong>Sample: Construct a ClientCertificateCredential behind a proxy</strong></p>
 * <pre>
 * ClientCertificateCredential credential2 = new ClientCertificateCredentialBuilder&#40;&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .pfxCertificate&#40;&quot;C:&#92;&#92;fakepath&#92;&#92;cert.pfx&quot;, &quot;P&#123;@literal @&#125;s$w0rd&quot;&#41;
 *     .proxyOptions&#40;new ProxyOptions&#40;Type.HTTP, new InetSocketAddress&#40;&quot;10.21.32.43&quot;, 5465&#41;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 */
@Immutable
public class ClientCertificateCredential implements TokenCredential {
    private final String clientCertificate;
    private final String clientCertificatePassword;
    private final IdentityClient identityClient;

    /**
     * Creates a ClientSecretCredential with default identity client options.
     * @param tenantId the tenant ID of the application
     * @param clientId the client ID of the application
     * @param certificatePath the PEM file or PFX file containing the certificate
     * @param certificatePassword the password protecting the PFX file
     * @param identityClientOptions the options to configure the identity client
     */
    ClientCertificateCredential(String tenantId, String clientId, String certificatePath, String certificatePassword,
                                IdentityClientOptions identityClientOptions) {
        Objects.requireNonNull(certificatePath, "'certificatePath' cannot be null.");
        this.clientCertificate = certificatePath;
        this.clientCertificatePassword = certificatePassword;
        identityClient =
            new IdentityClientBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .identityClientOptions(identityClientOptions)
                .build();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        if (clientCertificatePassword != null) {
            return identityClient.authenticateWithPfxCertificate(clientCertificate, clientCertificatePassword, request);
        } else {
            return identityClient.authenticateWithPemCertificate(clientCertificate, request);
        }
    }
}
