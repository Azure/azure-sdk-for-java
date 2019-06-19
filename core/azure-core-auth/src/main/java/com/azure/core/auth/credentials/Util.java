// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.auth.credentials;

import com.azure.core.credentials.AccessToken;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Util {
    static AuthenticationCallback authenticationDelegate(final MonoSink<AuthenticationResult> callback) {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(Object o) {
                callback.success((AuthenticationResult) o);
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.error(throwable);
            }
        };
    }

    static AsymmetricKeyCredential createAsymmetricKeyCredential(String clientId, byte[] clientCertificate, String clientCertificatePassword) {
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


    static PrivateKey privateKeyFromPem(String pem) {
        Pattern pattern = Pattern.compile("(?s)-----BEGIN PRIVATE KEY-----.*-----END PRIVATE KEY-----");
        Matcher matcher = pattern.matcher(pem);
        matcher.find();
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

    static X509Certificate publicKeyFromPem(String pem) {
        Pattern pattern = Pattern.compile("(?s)-----BEGIN CERTIFICATE-----.*-----END CERTIFICATE-----");
        Matcher matcher = pattern.matcher(pem);
        matcher.find();
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            InputStream stream = new ByteArrayInputStream(matcher.group().getBytes(StandardCharsets.UTF_8));
            return (X509Certificate) factory.generateCertificate(stream);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    static AccessToken parseAdal4jAuthenticationResult(AuthenticationResult result) {
        String token = result.getAccessToken();
        OffsetDateTime expiresOn = OffsetDateTime.ofInstant(result.getExpiresOnDate().toInstant(), ZoneOffset.UTC);
        return new AccessToken(token, expiresOn);
    }

    static AccessToken parseMSIToken(MSIToken result) {
        return new AccessToken(result.accessToken(), result.expiresOn());
    }

    static AccessToken parseAzureCliToken(AzureCliToken result) {
        return new AccessToken(result.accessToken(), result.expiresOn().atOffset(ZoneOffset.UTC));
    }

    private Util() { }
}
