/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config.auth;

import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.aad.adal4j.ClientCredential;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Default implementation of {@link AuthenticationExecutorFactory}.
 */
public class DefaultAuthenticationExecutorFactory implements AuthenticationExecutorFactory {
    private static final String CREDENTIAL_NOT_COMPLETE = "Credentials configuration is not complete. " +
            "Either clientCertificate or clientSecret should be configured for Azure Key Vault authentication.";
    private static final String READ_CERT_FAILED =
            "Failed to read client certificate for Azure Key Vault authentication.";

    @Override
    public AuthenticationExecutor create(Credentials credentials) {
        final String clientId = credentials.getClientId();

        // Try to authenticate with certificate at first.
        final Resource clientCertificate = credentials.getClientCertificate();
        if (clientCertificate != null) {
            return create(clientId, clientCertificate);
        }

        // Fallback to authenticate with secret.
        final String clientSecret = credentials.getClientSecret();
        if (!StringUtils.isEmpty(clientSecret)) {
            return create(clientId, clientSecret);
        }

        // No valid authentication found.
        throw new IllegalArgumentException(CREDENTIAL_NOT_COMPLETE);
    }

    private static AuthenticationExecutor create(String clientId, Resource clientCertificate) {
        try {
            AsymmetricKeyCredential credential = createAsymmetricKeyCredential(clientId, clientCertificate);
            return new CertificateAuthenticationExecutor(credential);
        } catch (IOException ex) {
            throw new IllegalStateException(READ_CERT_FAILED, ex);
        }
    }

    private static AuthenticationExecutor create(String clientId, String clientSecret) {
        final ClientCredential credential =  new ClientCredential(clientId, clientSecret);
        return new SecretAuthenticationExecutor(credential);
    }

    private static AsymmetricKeyCredential createAsymmetricKeyCredential(String clientId, Resource clientCertificate)
            throws IOException {
        final String pem = StreamUtils.copyToString(clientCertificate.getInputStream(), Charset.defaultCharset());
        final X509Certificate certificate = CertificateUtils.readX509CertificateFromPem(pem);
        final PrivateKey privateKey = CertificateUtils.readPrivateKeyFromPem(pem);
        return AsymmetricKeyCredential.create(clientId, privateKey, certificate);
    }
}
