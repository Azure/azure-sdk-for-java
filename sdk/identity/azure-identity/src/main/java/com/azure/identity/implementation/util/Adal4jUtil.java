// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.implementation.util.Base64Util;
import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.aad.adal4j.AuthenticationCallback;
import com.microsoft.aad.adal4j.AuthenticationResult;
import reactor.core.Exceptions;
import reactor.core.publisher.MonoSink;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for various operations for interacting with adal4j.
 */
public final class Adal4jUtil {
    /**
     * Routes a callback based call in adal4j to a Mono emitter.
     * @param callback the Mono emitter
     * @return the callback to pass into adal4j
     */
    public static AuthenticationCallback<AuthenticationResult> authenticationDelegate(final MonoSink<AuthenticationResult> callback) {
        return new AuthenticationCallback<AuthenticationResult>() {
            @Override
            public void onSuccess(AuthenticationResult o) {
                callback.success(o);
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.error(throwable);
            }
        };
    }

    /**
     * Creates an AsymmetricKeyCredential from a PKCS12 certificate.
     * @param clientId the client ID of the application.
     * @param clientCertificate the PKCS12 certificate
     * @param clientCertificatePassword the password protecting the PKCS12 certificate
     * @return the AsymmetricKeyCredential
     */
    public static AsymmetricKeyCredential createAsymmetricKeyCredential(String clientId, byte[] clientCertificate, String clientCertificatePassword) {
        try {
            return AsymmetricKeyCredential.create(clientId, new ByteArrayInputStream(clientCertificate), clientCertificatePassword);
        } catch (KeyStoreException kse) {
            throw  Exceptions.propagate(kse);
        } catch (NoSuchProviderException nspe) {
            throw  Exceptions.propagate(nspe);
        } catch (NoSuchAlgorithmException nsae) {
            throw  Exceptions.propagate(nsae);
        } catch (CertificateException ce) {
            throw  Exceptions.propagate(ce);
        } catch (IOException ioe) {
            throw  Exceptions.propagate(ioe);
        } catch (UnrecoverableKeyException uke) {
            throw  Exceptions.propagate(uke);
        }
    }

    /**
     * Extracts the PrivateKey from a PEM certificate.
     * @param pem the contents of a PEM certificate.
     * @return the PrivateKey
     */
    public static PrivateKey privateKeyFromPem(byte[] pem) {
        Pattern pattern = Pattern.compile("(?s)-----BEGIN PRIVATE KEY-----.*-----END PRIVATE KEY-----");
        Matcher matcher = pattern.matcher(new String(pem, StandardCharsets.UTF_8));
        if (!matcher.find()) {
            throw new IllegalArgumentException("Certificate file provided is not a valid PEM file.");
        }
        String base64 = matcher.group()
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\n", "")
                .replace("\r", "");
        byte[] key = Base64Util.decode(base64.getBytes(StandardCharsets.UTF_8));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extracts the X509Certificate certificate from a PEM certificate.
     * @param pem the contents of a PEM certificate.
     * @return the X509Certificate certificate
     */
    public static X509Certificate publicKeyFromPem(byte[] pem) {
        Pattern pattern = Pattern.compile("(?s)-----BEGIN CERTIFICATE-----.*-----END CERTIFICATE-----");
        Matcher matcher = pattern.matcher(new String(pem, StandardCharsets.UTF_8));
        if (!matcher.find()) {
            throw new IllegalArgumentException("PEM certificate provided does not contain -----BEGIN CERTIFICATE-----END CERTIFICATE----- block");
        }
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            InputStream stream = new ByteArrayInputStream(matcher.group().getBytes(StandardCharsets.UTF_8));
            return (X509Certificate) factory.generateCertificate(stream);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    private Adal4jUtil() { }
}
