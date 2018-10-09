/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2.credentials;

import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.v2.AzureEnvironment;
import com.microsoft.rest.v2.util.Base64Util;
import io.reactivex.Single;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Token based credentials for use with a REST Service Client.
 */
public class ApplicationTokenCredentials extends AzureTokenCredentials {
    /** A mapping from resource endpoint to its cached access token. */
    private Map<String, AuthenticationResult> tokens;
    /** The active directory application client id. */
    private String clientId;
    /** The authentication secret for the application. */
    private String clientSecret;
    /** The PKCS12 certificate byte array. */
    private byte[] clientCertificate;
    /** The certificate password. */
    private String clientCertificatePassword;

    /**
     * Initializes a new instance of the ApplicationTokenCredentials.
     *
     * @param clientId the active directory application client id.
     * @param domain the domain or tenant id containing this application.
     * @param secret the authentication secret for the application.
     * @param environment the Azure environment to authenticate with.
     *                    If null is provided, AzureEnvironment.AZURE will be used.
     */
    public ApplicationTokenCredentials(String clientId, String domain, String secret, AzureEnvironment environment) {
        super(environment, domain); // defer token acquisition
        this.clientId = clientId;
        this.clientSecret = secret;
        this.tokens = new HashMap<>();
    }

    /**
     * Initializes a new instance of the ApplicationTokenCredentials.
     *
     * @param clientId the active directory application client id.
     * @param domain the domain or tenant id containing this application.
     * @param certificate the PKCS12 certificate file content
     * @param password the password to the certificate file
     * @param environment the Azure environment to authenticate with.
     *                    If null is provided, AzureEnvironment.AZURE will be used.
     */
    public ApplicationTokenCredentials(String clientId, String domain, byte[] certificate, String password, AzureEnvironment environment) {
        super(environment, domain);
        this.clientId = clientId;
        this.clientCertificate = certificate;
        this.clientCertificatePassword = password;
        this.tokens = new HashMap<>();
    }

    /**
     * Initializes the credentials based on the provided credentials file.
     *
     * @param credentialsFile A  file with credentials, using the standard Java properties format.
     * and the following keys:
     *     subscription=&lt;subscription-id&gt;
     *     tenant=&lt;tenant-id&gt;
     *     client=&lt;client-id&gt;
     *     key=&lt;client-key&gt;
     *     managementURI=&lt;management-URI&gt;
     *     baseURL=&lt;base-URL&gt;
     *     authURL=&lt;authentication-URL&gt;
     * or a JSON format and the following keys
     * {
     *     "clientId": "&lt;client-id&gt;",
     *     "clientSecret": "&lt;client-key&gt;",
     *     "subscriptionId": "&lt;subscription-id&gt;",
     *     "tenantId": "&lt;tenant-id&gt;",
     * }
     * and any custom endpoints listed in {@link AzureEnvironment}.
     *
     * @return The credentials based on the file.
     * @throws IOException exception thrown from file access errors.
     */
    public static ApplicationTokenCredentials fromFile(File credentialsFile) throws IOException {
        return AuthFile.parse(credentialsFile).generateCredentials();
    }

    /**
     * Gets the active directory application client id.
     *
     * @return the active directory application client id.
     */
    public String clientId() {
        return clientId;
    }

    String clientSecret() {
        return clientSecret;
    }

    byte[] clientCertificate() {
        return clientCertificate;
    }

    String clientCertificatePassword() {
        return clientCertificatePassword;
    }

    @Override
    public synchronized Single<String> getToken(String resource) {
        AuthenticationResult authenticationResult = tokens.get(resource);
        if (authenticationResult != null && authenticationResult.getExpiresOnDate().after(new Date())) {
            return Single.just(authenticationResult.getAccessToken());
        } else {
            return acquireAccessToken(resource)
                    .map(ar -> {
                        tokens.put(resource, ar);
                        return ar.getAccessToken();
                    });
        }
    }

    private Single<AuthenticationResult> acquireAccessToken(String resource) {
        String authorityUrl = this.environment().activeDirectoryEndpoint() + this.domain();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return Single.defer(() -> {
            AuthenticationContext context = new AuthenticationContext(authorityUrl, false, executor);
            if (proxy() != null) {
                context.setProxy(proxy());
            }
            if (clientSecret != null) {
                return Single.fromFuture(context.acquireToken(
                        resource,
                        new ClientCredential(this.clientId(), clientSecret),
                        null));
            } else if (clientCertificate != null && clientCertificatePassword != null) {
                return Single.fromFuture(context.acquireToken(
                        resource,
                        AsymmetricKeyCredential.create(clientId, new ByteArrayInputStream(clientCertificate), clientCertificatePassword),
                        null));
            } else if (clientCertificate != null) {
                return Single.fromFuture(context.acquireToken(
                        resource,
                        AsymmetricKeyCredential.create(clientId(), privateKeyFromPem(new String(clientCertificate)), publicKeyFromPem(new String(clientCertificate))),
                        null));
            }
            throw new AuthenticationException("Please provide either a non-null secret or a non-null certificate.");
        }).doFinally(executor::shutdown);
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
            InputStream stream = new ByteArrayInputStream(matcher.group().getBytes());
            return (X509Certificate) factory.generateCertificate(stream);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}
