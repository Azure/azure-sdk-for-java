// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * An {@link RSAKey} resolver function implementation parses the certificate locally.
 *
 * @since 4.3.0
 * @see <a href="https://docs.microsoft.com/azure/active-directory/develop/active-directory-certificate-credentials">Certificate credentials</a>
 */
public class AadOAuth2ClientAuthenticationJWKResolver implements OAuth2ClientAuthenticationJWKResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadOAuth2ClientAuthenticationJWKResolver.class);

    private final String clientCertificatePath;
    private final String clientCertificatePassword;

    /**
     * Creates a new instance of {@link AadOAuth2ClientAuthenticationJWKResolver}
     * @param clientCertificatePath the client certificate path
     * @param clientCertificatePassword the client certificate password
     */
    public AadOAuth2ClientAuthenticationJWKResolver(String clientCertificatePath,
                                                    String clientCertificatePassword) {
        Assert.notNull(clientCertificatePath, "clientCertificatePath cannot be null");
        Assert.notNull(clientCertificatePassword, "clientCertificatePassword cannot be null");

        String fileExtension = clientCertificatePath.substring(clientCertificatePath.lastIndexOf(".") + 1);
        Assert.isTrue("pfx".equals(fileExtension) || "p12".equals(fileExtension),
            "Only files with the '.pfx' or '.p12' extension are supported.");

        this.clientCertificatePath = clientCertificatePath;
        this.clientCertificatePassword = clientCertificatePassword;
    }

    @Override
    public JWK resolve(ClientRegistration clientRegistration) {
        if (ClientAuthenticationMethod.PRIVATE_KEY_JWT.equals(clientRegistration.getClientAuthenticationMethod())) {
            try (FileInputStream inputStream = new FileInputStream(clientCertificatePath)) {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                char[] password = clientCertificatePassword.toCharArray();
                keyStore.load(inputStream, password);
                String alias = keyStore.aliases().nextElement();
                PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password);
                X509Certificate x509Certificate = (X509Certificate) keyStore.getCertificate(alias);
                PublicKey publicKey = x509Certificate.getPublicKey();
                return new RSAKey.Builder((RSAPublicKey) publicKey)
                    .privateKey(privateKey)
                    .x509CertThumbprint(Base64URL.encode(getX5t(x509Certificate)))
                    .keyID(UUID.randomUUID().toString())
                    .build();
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException
                     | CertificateException | UnrecoverableKeyException e) {
                LOGGER.error("Resolve RSAKey exception.", e);
            }
        }
        return null;
    }

    private byte[] getX5t(X509Certificate cert)
        throws NoSuchAlgorithmException, CertificateEncodingException {
        return getSHA1Byte(cert.getEncoded());
    }

    private byte[] getSHA1Byte(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(data);
        return digest.digest();
    }
}
