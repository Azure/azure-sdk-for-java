package com.azure.identity.credential;

import com.azure.identity.AccessToken;
import com.azure.identity.IdentityClient;
import com.azure.identity.IdentityClientOptions;
import reactor.core.publisher.Mono;

/**
 * An AAD credential that acquires a token with a client certificate for an AAD application.
 */
public class ClientCertificateCredential extends AadCredential<ClientCertificateCredential> {
    private String clientCertificate;
    private String clientCertificatePassword;
    private IdentityClient identityClient;

    /**
     * Creates a ClientSecretCredential with default AAD endpoint https://login.microsoftonline.com.
     */
    public ClientCertificateCredential() {
        this(new IdentityClientOptions());
    }

    public ClientCertificateCredential(IdentityClientOptions identityClientOptions) {
        this.identityClient = new IdentityClient(identityClientOptions);
    }

    /**
     * Sets the client certificate for authenticating to AAD.
     * @param certificatePath the PEM file containing the certificate
     * @return the credential itself
     */
    public ClientCertificateCredential pemCertificate(String certificatePath) {
        this.clientCertificate = certificatePath;
        return this;
    }

    /**
     * Sets the client certificate for authenticating to AAD.
     * @param certificatePath the password protected PFX file containing the certificate
     * @param clientCertificatePassword the password protecting the PFX file
     * @return the credential itself
     */
    public ClientCertificateCredential pfxCertificate(String certificatePath, String clientCertificatePassword) {
        this.clientCertificate = certificatePath;
        this.clientCertificatePassword = clientCertificatePassword;
        return this;
    }

    @Override
    public Mono<String> getToken(String... scopes) {
        validate();
        if (clientCertificate == null) {
            return Mono.error(new IllegalArgumentException("Non-null value must be provided for clientCertificate property in ClientCertificateCredential"));
        }
        if (clientCertificatePassword != null) {
            return identityClient.activeDirectory().acquireTokenWithPfxCertificate(tenantId(), clientId(), clientCertificate, clientCertificatePassword, scopes).map(AccessToken::token);
        } else {
            return identityClient.activeDirectory().acquireTokenWithPemCertificate(tenantId(), clientId(), clientCertificate, scopes).map(AccessToken::token);
        }
    }
}
